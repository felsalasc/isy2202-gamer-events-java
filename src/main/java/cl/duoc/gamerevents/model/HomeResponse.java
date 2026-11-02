package cl.duoc.gamerevents.model;

import java.util.List;

public record HomeResponse(List<EventSummary> upcoming, List<EventSummary> featured, List<BrandAdResponse> ads) {
}
