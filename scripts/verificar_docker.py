#!/usr/bin/env python3
"""Verificacion de la entrega S8 con los contenedores ya levantados."""
import base64
import datetime
import json
from pathlib import Path
import ssl
import subprocess
import sys
import time
import urllib.error
import urllib.parse
import urllib.request

ROOT = Path(__file__).resolve().parent.parent
EVIDENCE = ROOT / "evidencias" / "semana8"
COMPOSE = ["docker", "compose", "-p", "banco-xyz-s8", "-f", "docker-compose.yaml"]
EXPECTED = {"config-server", "discovery-server", "auth-server", "banco-api",
            "bff-web", "bff-movil", "bff-cajeros", "auditoria-service", "broker"}

def say(text):
    print(text, flush=True)

def require(condition, message):
    if not condition:
        raise RuntimeError(message)

def secrets_env():
    values = {}
    for line in (ROOT / ".env").read_text().splitlines():
        if line.strip() and not line.startswith("#"):
            key, value = line.split("=", 1)
            values[key] = value.strip()
    return values

def scrub(text, values):
    for key, value in values.items():
        if ("SECRET" in key or "PASSWORD" in key) and value:
            text = text.replace(value, "[OCULTO]")
    return text

def compose(*args):
    result = subprocess.run(COMPOSE + list(args), cwd=ROOT, text=True,
                            stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
    require(result.returncode == 0, "Docker Compose: " + result.stdout[-1500:])
    return result.stdout

def request(url, token=None, form=None, client=None, method=None, headers=None):
    hdr = dict(headers or {})
    if token:
        hdr["Authorization"] = "Bearer " + token
    if client:
        hdr["Authorization"] = "Basic " + base64.b64encode((client[0] + ":" + client[1]).encode()).decode()
    data = None
    if form is not None:
        hdr["Content-Type"] = "application/x-www-form-urlencoded"
        data = urllib.parse.urlencode(form).encode()
    req = urllib.request.Request(url, data=data, headers=hdr, method=method)
    context = ssl._create_unverified_context() if url.startswith("https://localhost:") else None
    try:
        with urllib.request.urlopen(req, context=context, timeout=10) as response:
            return response.status, response.read().decode()
    except urllib.error.HTTPError as error:
        return error.code, error.read().decode()

def wait_health(url, timeout=120):
    deadline = time.monotonic() + timeout
    while time.monotonic() < deadline:
        try:
            status, _ = request(url)
            if status == 200:
                return
        except (urllib.error.URLError, OSError, TimeoutError):
            pass
        time.sleep(2)
    raise RuntimeError("No esta disponible: " + url)

def docker_rows():
    raw = compose("ps", "--format", "json").strip()
    if raw.startswith("["):
        return json.loads(raw)
    return [json.loads(line) for line in raw.splitlines() if line.strip()]

def main():
    EVIDENCE.mkdir(parents=True, exist_ok=True)
    values = secrets_env()
    summary = EVIDENCE / "05_pruebas_docker_jms.txt"
    summary.write_text("VERIFICACION DOCKER Y JMS BANCO XYZ S8\nFecha: " +
        datetime.datetime.now().astimezone().isoformat() + "\n\n")
    results, paused = [], set()
    def record(label, ok, detail=""):
        line = f"{'OK' if ok else 'FALLO'} | {label}" + (f" | {detail}" if detail else "")
        say(line)
        with summary.open("a") as out:
            out.write(line + "\n")
        results.append({"prueba": label, "ok": bool(ok), "detalle": detail})
        require(ok, "Fallo: " + label)
    def emit(id_value, token):
        status, body = request(f"http://localhost:8100/api/transacciones/{id_value}/eventos",
                               token=token, method="POST")
        record("Publicacion asincrona de transaccion " + id_value, status == 202, "HTTP " + str(status))
        data = json.loads(body)
        require(data.get("estado") == "EN_COLA" and data.get("eventoId"), "Respuesta de evento inesperada.")
        return data
    def await_event(event, token):
        deadline = time.monotonic() + 45
        body, status = "", 0
        while time.monotonic() < deadline:
            status, body = request("http://localhost:8104/auditoria/eventos/" + event["eventoId"], token=token)
            if status == 200:
                break
            time.sleep(1)
        data = json.loads(body) if status == 200 else {}
        record("Consumidor procesa evento " + event["eventoId"], status == 200 and
               data.get("transaccionId") == event["transaccionId"], "HTTP " + str(status))
        (EVIDENCE / ("05_evento_" + event["eventoId"] + ".json")).write_text(
            json.dumps({"publicacion": event, "procesamiento": data}, indent=2))
        return data
    try:
        rows = docker_rows()
        record("Nueve contenedores activos y saludables",
            {r["Service"] for r in rows} == EXPECTED and
            all(r.get("State") == "running" and r.get("Health") == "healthy" for r in rows))
        record("Ocho imagenes de microservicios",
            len({r["Image"] for r in rows if r["Service"] != "broker"}) == 8)
        tokens = {}
        for channel in ("web", "movil", "cajeros"):
            scope = f"banco.read canal.{channel}"
            if channel != "movil":
                scope += " transacciones.write"
            status, body = request("http://localhost:9000/oauth2/token",
                form={"grant_type": "client_credentials", "scope": scope},
                client=("cliente-" + channel, values["AUTH_" + channel.upper() + "_SECRET"]))
            record("OAuth2 emite JWT en Docker para " + channel, status == 200)
            tokens[channel] = json.loads(body)["access_token"]
        routes = {"web": "https://localhost:8101/web/intereses?cuentaId=137",
                  "movil": "https://localhost:8102/movil/cuentas/137",
                  "cajeros": "https://localhost:8103/cajeros/saldo/137"}
        for channel, url in routes.items():
            deadline = time.monotonic() + 70
            while time.monotonic() < deadline:
                status, body = request(url, token=tokens[channel])
                if status == 200:
                    break
                time.sleep(3)
            record("BFF " + channel + " obtiene datos desde contenedores", status == 200)
        status, body = request("https://localhost:8101/actuator/info", token=tokens["web"])
        info = json.loads(body) if status == 200 else {}
        record("Configuracion central en Docker",
            info.get("proyecto", {}).get("configuracion") == "config-server-semana8")
        status, body = request("http://localhost:8761/eureka/apps",
            client=("infra", values["INFRA_SECRET"]), headers={"Accept": "application/json"})
        require(status == 200, "No se pudo consultar Eureka.")
        apps = json.loads(body).get("applications", {}).get("application", [])
        if isinstance(apps, dict):
            apps = [apps]
        registered = {item.get("name") for item in apps}
        record("Eureka incluye el consumidor de eventos",
            {"BANCO-API", "BFF-WEB", "BFF-MOVIL", "BFF-CAJEROS", "AUDITORIA-SERVICE"}.issubset(registered),
            ", ".join(sorted(registered)))
        (EVIDENCE / "05_eureka.json").write_text(body)
        status, _ = request("http://localhost:8104/auditoria/eventos")
        record("Auditoria protegida sin token", status == 401, "HTTP " + str(status))
        status, _ = request("http://localhost:8100/api/transacciones/1/eventos", method="POST")
        record("Publicacion protegida sin token", status == 401, "HTTP " + str(status))
        status, _ = request("http://localhost:8100/api/transacciones/1/eventos",
                            token=tokens["movil"], method="POST")
        record("Publicacion exige permiso transacciones.write", status == 403, "HTTP " + str(status))
        first = emit("1", tokens["web"])
        first_data = await_event(first, tokens["web"])
        payload = json.loads(first_data["contenido"])
        record("Evento conserva datos de la transaccion original",
            payload["transaccion"]["id"] == "1" and payload["transaccion"]["monto"] == "3000")
        say("Deteniendo auditoria-service: el broker debe conservar el proximo evento...")
        compose("stop", "auditoria-service")
        paused.add("auditoria-service")
        pending = emit("2", tokens["web"])
        record("API acepta eventos con consumidor detenido", pending["estado"] == "EN_COLA")
        compose("start", "auditoria-service")
        paused.remove("auditoria-service")
        wait_health("http://localhost:8104/actuator/health")
        await_event(pending, tokens["web"])
        status, _ = request("http://localhost:8104/auditoria/eventos/" + first["eventoId"], token=tokens["web"])
        record("Auditoria conserva eventos al reiniciar", status == 200)
        say("Comprobando Circuit Breaker WEB con la API detenida en Docker...")
        compose("stop", "banco-api")
        paused.add("banco-api")
        last_body = {}
        statuses = []
        for _ in range(6):
            status, body = request(routes["web"], token=tokens["web"])
            statuses.append(status)
            if status == 503:
                last_body = json.loads(body)
        record("Fallback WEB en Docker", statuses == [503] * 6 and last_body.get("estado") == "DEGRADADO")
        metric_url = "https://localhost:8101/actuator/metrics/resilience4j.circuitbreaker.state?tag=name:bancoApi&tag=state:"
        status, body = request(metric_url + "open", token=tokens["web"])
        metric = json.loads(body) if status == 200 else {}
        record("Circuito WEB OPEN en Docker", any(m.get("value") == 1.0 for m in metric.get("measurements", [])))
        compose("start", "banco-api")
        paused.remove("banco-api")
        wait_health("http://localhost:8100/actuator/health")
        deadline, success = time.monotonic() + 70, 0
        while time.monotonic() < deadline:
            status, _ = request(routes["web"], token=tokens["web"])
            success = success + 1 if status == 200 else 0
            if success >= 2:
                break
            time.sleep(2)
        status, body = request(metric_url + "closed", token=tokens["web"])
        metric = json.loads(body) if status == 200 else {}
        record("WEB recupera datos y circuito CLOSED en Docker",
            success >= 2 and any(m.get("value") == 1.0 for m in metric.get("measurements", [])))
        status, _ = request(routes["web"])
        record("OAuth2 se mantiene tras recuperar API", status == 401)
        # Esperar a que Docker actualice el ultimo healthcheck despues de los reinicios.
        deadline = time.monotonic() + 45
        while time.monotonic() < deadline:
            rows = docker_rows()
            if len(rows) == 9 and all(r.get("Health") == "healthy" for r in rows):
                break
            time.sleep(2)
        record("Estado final: nueve servicios saludables",
            {r["Service"] for r in rows} == EXPECTED and all(
                r.get("State") == "running" and r.get("Health") == "healthy" for r in rows))
        say("DOCKER Y JMS VERIFICADOS.")
        say("Los contenedores quedan activos para revisar la entrega.")
    finally:
        for name in paused:
            try:
                compose("start", name)
            except Exception as error:
                say("No fue posible reiniciar " + name + ": " + str(error))
        for filename, args in (
            ("05_estado_docker.txt", ("ps",)),
            ("05_imagenes_docker.txt", ("images",)),
            ("05_logs_docker.log", ("logs", "--no-color", "--tail", "160"))):
            try:
                (EVIDENCE / filename).write_text(scrub(compose(*args), values))
            except Exception as error:
                say("Registro pendiente: " + filename + " (" + str(error) + ")")
        (EVIDENCE / "05_resultados_docker_jms.json").write_text(json.dumps(results, indent=2))
        say("Evidencias: " + str(summary))

if __name__ == "__main__":
    try:
        main()
    except KeyboardInterrupt:
        say("Verificacion interrumpida.")
        sys.exit(130)
    except Exception as error:
        say("ERROR: " + scrub(str(error), secrets_env()))
        sys.exit(1)
