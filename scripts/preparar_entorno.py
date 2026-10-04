#!/usr/bin/env python3
from pathlib import Path
import secrets
root = Path(__file__).resolve().parent.parent
path = root / ".env"
text = path.read_text() if path.exists() else ""
keys = {line.split("=", 1)[0] for line in text.splitlines() if "=" in line and not line.startswith("#")}
for key in ("AUTH_WEB_SECRET", "AUTH_MOVIL_SECRET", "AUTH_CAJEROS_SECRET", "INFRA_SECRET", "JMS_PASSWORD"):
    if key not in keys:
        text = text.rstrip() + "\n" + key + "=" + secrets.token_urlsafe(24) + "\n"
path.write_text(text.lstrip())
path.chmod(0o600)
print("Configuracion local preparada.")
