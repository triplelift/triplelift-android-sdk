# Getting Started

Try out our [demo](https://s3.amazonaws.com/mobile.triplelift.com/triplelift-android-sdk.zip)

## Adding the TripleLift SDK to your project

Clone this project or use jitpack to add the TripleLift SDK as a dependency.

````
repositories { 
    ...
    maven { url "https://jitpack.io" }
}
dependencies {
    compile 'com.github.triplelift:triplelift-android-sdk:2.0'
}
````

## Requirements and Dependencies

The SDK uses Volley for requests and requires that it be an included module: http://developer.android.com/training/volley/index.html

## Update the Android Manifest

````xml
// Required
<uses-permission android:name="android.permission.INTERNET"/>
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
// Optional 
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/>
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>  
````

We need both ````INTERNET```` and ````ACCESS_NETWORK_STATE```` in order to request ads from TripleLift. Location services (use ````ACCESS_FINE_LOCATION```` or ````ACCESS_COARSE_LOCATION````) are optional, but will improve performance on user-targeting based ad campaigns. ````WRITE_EXTERNAL_STORAGE```` is optional but will help improve the effectiveness of caching with respect to TripleLift native ads.

## Design an XML Layout to Encapsulate Native Ads

Use XML to define a placement in your feed. A quick way to get started is to use an existing list element layout as a template and include the TripleLift native ad fields:

Name | View Type
-----|----------
Title | TextView
Main Image | ImageView
Logo (optional) | ImageView
Caption | TextView
Brand Name | TextView

Example:

````xml
<!-- native_ad_element.xml -->
<RelativeLayout>
  <ImageView android:id="@+id/native_ad_main_image"/>
  <ImageView android:id="@+id/native_ad_logo"/>
  <TextView android:id="@+id/native_ad_title"/>
  <TextView android:id="@+id/native_ad_caption"/>
  <TextView android:id="@+id/native_ad_brand_name"/>
</RelativeLayout>
````

NOTE: You should set the background property on your ImageViews to null in order to handle PNG transparency gracefully:

````xml
<ImageView android:background="@null"/>
````

## Instantiate the NativeAdAdapter

To delegate the insertion of native ads by the TripleLift SDK into your ListView bind the layout you created above using a NativeAdLayout, and instantiate the NativeAdAdapter within your activity using the current ````Context````, implemented ````BaseAdapter````, your xml```<INVENTORY_CODE>```` (provided by your TripleLift Account Manager), ````NativeAdLayout````, and default initial position as well as the repeat interval for ads to be rendered within the ListView. NOTE: Version 1.3 will allow changes to be made dynamically with respect to the initial position, repeat interval, as well as an added field for fixed positions.

````java
// Include in the body of onCreate
NativeAdLayout nativeAdLayout = new NativeAdLayout(R.layout.native_ad_item,
        R.id.native_ad_title, R.id.native_ad_image, R.id.native_ad_logo, 
        R.id.native_ad_caption, R.id.native_ad_brand_name);

nativeAdAdapter = new NativeAdAdapter(context,
        adapter, <INVENTORY_CODE>, nativeAdLayout, 
        initPosition, interval);
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

## Prevent Memory Leaks
Call ````onDestroy()```` on the NativeAdAdapter when destroying the hosting activity to prevent memory leaks.

````java
// Include in the body of onDestroy
nativeAdAdapter.onDestroy();
super.onDestroy();
````
