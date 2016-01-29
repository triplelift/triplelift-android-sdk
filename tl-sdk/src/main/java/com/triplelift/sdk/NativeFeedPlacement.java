package com.triplelift.sdk;

public class NativeFeedPlacement {

    private static final int CAPACITY = 50;
    private static final int NOT_FOUND = -1;

    private final NativeAd[] placedNativeAds = new NativeAd[CAPACITY];
    int[] positionsToInsert = new int[CAPACITY];
    int[] positionsLive = new int[CAPACITY];
    int adsLive = 0;

    protected NativeFeedPlacement(NativeFeedPositions nativeFeedPositions) {
        int repeatInterval = nativeFeedPositions.getRepeatInterval();
        int[] fixedPositions = nativeFeedPositions.getFixedPositions();

        int current = 0;
        int adsPlaced = 0;
        if (fixedPositions != null) {
            for (int position: fixedPositions) {
                if (position == 0) {
                    continue;
                }
                current = position - adsPlaced;
                positionsToInsert[adsPlaced++] = current;
            }
        }

        while (adsPlaced < CAPACITY) {
            current = current + repeatInterval - 1;
            positionsToInsert[adsPlaced++] = current;
        }
    }

    public NativeAd getNativeAd(int position) {

        int index = findIndex(positionsLive, position);
        if (index == NOT_FOUND) {
            return null;
        }

        return placedNativeAds[index];
    }

    public void placeNativeAd(NativeAd nativeAd, int position) {
        if (!isAdPosition(position) || isAdPositionLive(position)) {
            return;
        }

        int i = findIndex(positionsToInsert, position);
        while (i < positionsToInsert.length) {
            positionsToInsert[i++]++;
        }

        int j = 0;
        boolean insertedLiveAd = false;
        while (j < positionsLive.length) {
            if (!insertedLiveAd && positionsLive[j] == 0) {
                positionsLive[j] = position;
                placedNativeAds[j] = nativeAd;
                insertedLiveAd = true;
                adsLive++;
            }

            if (insertedLiveAd && positionsLive[j] == 0) {
                break;
            }

            if (insertedLiveAd) {
                positionsLive[j]++;
            }

            j++;
        }
    }

    public boolean isAdPosition(int position) {
        int index = findIndex(positionsToInsert, position);
        return index != NOT_FOUND;
    }

    public boolean isAdPositionLive(int position) {
        int index = findIndex(positionsLive, position);
        return index != NOT_FOUND;
    }

    public int adsLive() {
        return adsLive;
    }

    public int getContentPosition(int position) {
        int i = 0;

        while (i < adsLive && positionsLive[i] < position) {
            i++;
        }

        return position - i;
    }

    private int findIndex(int[] source, int position) {
        int i = 0;

        while (i < source.length) {
            if (source[i] == position) {
                return i;
            }
            i++;
        }

        return NOT_FOUND;
    }


}
