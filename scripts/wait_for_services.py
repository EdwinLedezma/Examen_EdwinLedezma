"""Espera a que ambos servicios y sus bases de datos estén disponibles."""
import json
import os
import time
from urllib.error import URLError, HTTPError
from urllib.request import urlopen

urls = [os.getenv("PARCEL_URL", "http://localhost:8081"), os.getenv("MONITORING_URL", "http://localhost:8082")]
deadline = time.monotonic() + 120
pending = set(urls)
while pending and time.monotonic() < deadline:
    for base in list(pending):
        try:
            with urlopen(base + "/actuator/health", timeout=2) as response:
                if json.load(response).get("status") == "UP":
                    pending.remove(base)
                    print("Disponible:", base, flush=True)
        except (URLError, HTTPError, TimeoutError, OSError):
            pass
    if pending:
        time.sleep(1)
if pending:
    raise SystemExit("No iniciaron a tiempo: " + ", ".join(sorted(pending)))
