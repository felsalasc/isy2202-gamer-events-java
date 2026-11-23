package cl.duoc.gamerevents.model;

import java.util.List;

public record CommunityPage(List<ShareResponse> items,int page,boolean hasNext) { }
