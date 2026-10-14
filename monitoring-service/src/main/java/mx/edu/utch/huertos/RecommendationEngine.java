package mx.edu.utch.huertos;

import java.util.*;
import org.springframework.stereotype.Component;

@Component
public class RecommendationEngine {
  public record State(
      String status, Reading latestReading, List<String> alerts, List<String> recommendations) {}

  public State evaluate(Reading r) {
    if (r == null)
      return new State(
          "SIN_DATOS", null, List.of(), List.of("Registra una lectura para evaluar la parcela."));
    List<String> alerts = new ArrayList<>(), recommendations = new ArrayList<>();
    if (r.soilMoisture < 30) {
      alerts.add("Humedad baja.");
      recommendations.add("Revisar riego y cobertura del suelo.");
    }
    if (r.soilMoisture > 80) {
      alerts.add("Humedad alta.");
      recommendations.add("Revisar drenaje y reducir riego.");
    }
    if (r.temperature < 5 || r.temperature > 35) {
      alerts.add("Temperatura fuera del intervalo orientativo.");
      recommendations.add("Evaluar protección del cultivo ante temperaturas extremas.");
    }
    if (r.ph < 5.5 || r.ph > 7.5) {
      alerts.add("pH fuera del intervalo orientativo.");
      recommendations.add("Solicitar análisis del suelo antes de aplicar correctivos.");
    }
    if (r.nitrogen < 20 || r.phosphorus < 10 || r.potassium < 50) {
      alerts.add("Posible deficiencia de nutrientes.");
      recommendations.add("Evaluar compostaje y rotación según análisis técnico.");
    }
    if (r.airQuality > 100) {
      alerts.add("Calidad del aire desfavorable.");
      recommendations.add("Revisar las condiciones antes de programar tareas al aire libre.");
    }
    if (alerts.isEmpty()) recommendations.add("Mantener monitoreo periódico.");
    return new State(alerts.isEmpty() ? "NORMAL" : "ALERTA", r, alerts, recommendations);
  }
}
