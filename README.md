# Getting Started

Try out our [demo](https://s3.amazonaws.com/mobile.triplelift.com/triplelift-android-sdk.zip)

## Adding the TripleLift SDK to your project

Clone this project and add it as a module in your android project.

## Requirements and Dependencies

The SDK uses Volley for requests and requires that it be an included module: http://developer.android.com/training/volley/index.html

## Update the Android Manifest

````xml
// Required
<uses-permission android:name="android.permission.INTERNET"/>
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
// Optional 
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/>
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>  
````

We need both ````INTERNET```` and ````ACCESS_NETWORK_STATE```` in order to request ads from TripleLift. Location services (use ````ACCESS_FINE_LOCATION```` or ````ACCESS_COARSE_LOCATION````) are optional, but will improve performance on user-targeting based ad campaigns. ````WRITE_EXTERNAL_STORAGE```` is optional but will help improve the effectiveness of caching with respect to TripleLift native ads.

## Design an XML Layout to Encapsulate Native Ads

Use XML to define a placement in your feed. A quick way to get started is to use an existing list element layout as a template and include the TripleLift native ad fields:

Name | View Type
-----|----------
Title | TextView
Main Image | com.android.volley.toolbox.NetworkImageView
Caption | TextView
Brand Name | TextView

Example:

````xml
<!-- native_ad_element.xml -->
<RelativeLayout>
  <ImageView android:id="@+id/native_ad_main_image"/>
  <TextView android:id="@+id/native_ad_heading"/>
  <TextView android:id="@+id/native_ad_caption"/>
  <TextView android:id="@+id/native_ad_advertiser"/>
</RelativeLayout>
````

NOTE: You should set the background property on your ImageViews to null in order to handle PNG transparency gracefully:

````xml
<ImageView android:background="@null"/>
````

# Single View Integration

## Instantiate the NativeAdUnit

To delegate the insertion of native ads by the TripleLift SDK into your Custom View bind the layout you created above using a NativeAdLayout object (for bindings), a frame layout (that you will append the ad to), and instantiate the NativeAdUnit within your activity using the current ````Context````, your ````<INVENTORY_CODE>```` (provided by your TripleLift Account Manager), and the ````NativeAdLayout```` id.

````java
// Include in the body of onCreate

// Include a placeholder for the ad
final FrameLayout frameLayout = (FrameLayout) findViewById(R.id.native_ad_placeholder);

NativeAdLayout nativeAdLayout = new NativeAdLayout(R.id.native_ad_advertiser,
                R.id.native_ad_image, R.id.native_ad_heading,
                R.id.native_ad_caption);

// Instantiate the unit
nativeAdUnit = new NativeAdUnit(getApplicationContext(), <INVENTORY_CODE>, R.layout.ad_item);

// Register the Native Ad Layout to add content bindings
nativeAdUnit.registerNativeAdLayout(nativeAdLayout);
````

## Request & Render Ads

By running ````nativeAdUnit.requestAds()````, you'll be asking the SDK to ping the TripleLift exchange, and you'll know an ad is available when ````nativeAdUnit.adIsAvailable()```` is ````true````, once it is true, you can call ````nativeAdUnit.getNativeAd(frameLayout)````, which you will append to the frame layout.

````java
frameLayout.removeAllViews();
frameLayout.addView(adView);
````

# ListView Adapter Integration

## Instantiate the NativeAdAdapter

To delegate the insertion of native ads by the TripleLift SDK into your List View bind the layout you created above using a NativeAdLayout, and instantiate the NativeAdUnit within your activity using the current ````Context````, implemented ````BaseAdapter````, your ````<INVENTORY_CODE>```` (provided by your TripleLift Account Manager), ````NativeAdLayout````, and default initial position as well as the repeat interval for ads to be rendered within the ListView. 

````java
// Include in the body of onCreate
NativeAdLayout nativeAdLayout = new NativeAdLayout(R.layout.native_ad_item,
        R.id.native_ad_heading, R.id.native_ad_image, R.id.native_ad_caption, 
        R.id.native_ad_advertiser);

nativeAdAdapter = new NativeAdAdapter(context,
        adapter, <INVENTORY_CODE>, nativeAdLayout, 
        initPosition, interval);
        
// Optional: set the aspect ratio for each image, default is the Golden Ratio
// otherwise, pass in width and height into the userData object in the nativeAdAdapter
nativeAdAdapter.setAspectRatio(aspectRatio);
````

## Request Native Ads
Call requestAds with a Map<String, Object> containing relevant user information that can drive up eCPM.

Field | Java Static Type
------|---------
location | android.location.Location
keywords | String 
gender | String
yob | Integer

NOTE: yob == year of birth and keywords is a comma delimited list of keywords.

````java
// Include in the body of onResume
Map<String, Object> userData; // Populate with the relevant user data
adapter.requestAds(userData);
super.onResume();
````
