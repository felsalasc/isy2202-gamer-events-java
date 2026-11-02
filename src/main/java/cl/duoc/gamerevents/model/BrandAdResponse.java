package cl.duoc.gamerevents.model;

public record BrandAdResponse(String name, String message) {

    public static BrandAdResponse from(BrandAd ad) {
        return new BrandAdResponse(ad.getName(), ad.getMessage());
    }
}
