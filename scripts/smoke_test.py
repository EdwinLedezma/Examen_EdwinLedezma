"""Prueba conjunta sin dependencias externas. Ejecutar con Compose encendido."""
import json
import os
import uuid
from datetime import datetime, timezone
from urllib.request import Request, urlopen
from urllib.error import HTTPError

def request(method, url, data=None, expected=200):
    raw = None if data is None else json.dumps(data).encode()
    req = Request(url, data=raw, method=method, headers={"Content-Type": "application/json"})
    try:
        with urlopen(req, timeout=15) as response:
            status, body = response.status, json.load(response)
    except HTTPError as exc:
        status, body = exc.code, json.load(exc)
    assert status == expected, (url, expected, status, body)
    return body

purl = os.getenv("PARCEL_URL", "http://localhost:8081") + "/api/v1/parcels"
rurl = os.getenv("MONITORING_URL", "http://localhost:8082") + "/api/v1/readings"
name = "Huerto " + uuid.uuid4().hex[:12]
payload = {"name": "  <b>" + name + "</b>  ", "location": {"latitude": 28.63, "longitude": -106.08}, "soilType": "LIMOSO", "ownerOrLead": "  Ana   Pérez "}
p = request("POST", purl, payload, 201)
assert p["name"] == name and p["ownerOrLead"] == "Ana Pérez"
assert any(x["id"] == p["id"] for x in request("GET", purl))
assert request("GET", purl + "/" + p["id"])["currentState"]["status"] == "SIN_DATOS"
reading = {"parcelId": p["id"], "soilMoisture": 10, "temperature": 25, "ph": 6.5, "nitrogen": 40, "phosphorus": 20, "potassium": 100, "airQuality": 50, "measuredAt": datetime.now(timezone.utc).isoformat()}
request("POST", rurl, reading, 201)
detail = request("GET", purl + "/" + p["id"])
assert detail["currentState"]["status"] == "ALERTA"
assert detail["currentState"]["recommendations"]
request("POST", purl, payload, 409)
bad = dict(payload, name="<script>alert(1)</script>")
assert "name" in request("POST", purl, bad, 400)["errors"]
request("GET", purl + "/no-uuid", expected=400)
request("GET", purl + "/" + str(uuid.uuid4()), expected=404)
request("POST", rurl, dict(reading, ph=15), 400)
request("POST", rurl, dict(reading, parcelId=str(uuid.uuid4())), 404)
print("OK: registro, sanitización, listado, estado, lectura, alerta y errores 400/404/409.")
