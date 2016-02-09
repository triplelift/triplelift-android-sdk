package com.triplelift.sdk;

public class NativeAdLayout {

    private final int brandId, imageId, headerId, captionId;//logo

    public NativeAdLayout(int brandId, int imageId,
                          int headerId, int captionId) {
        this.brandId = brandId;
        this.imageId = imageId;
        //this.logoId = logoId;
        this.headerId = headerId;
        this.captionId = captionId;
    }

    public int getBrandId() {
        return brandId;
    }

    public int getImageId() {
        return imageId;
    }

//    public int getLogoId() {
//        return logoId;
//    }

    public int getHeaderId() {
        return headerId;
    }

    public int getCaptionId() {
        return captionId;
    }
}
