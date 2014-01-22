triplelift-android-sdk
======================

The TripleLift Android SDK assists in implementing Sponsored Content (SC) images into Android applications.

Each native instance is given an "inventory code" which is used to instantiate a TripleLift Sponsored Content Image Factory which is responsible for retrieving a set of sponsored images from an advertiser. The Image Factory will run an auction on the AppNexus platform and return an image from the winning advertiser.

The sponsored image object has the following attributes:

- heading: a header describing the image
- caption: the image's descriptive caption
- clickthroughLink: the url the image should click through to

In addition the Sponsored Content Image class has methods for retrieving the image url or a Bitmap object.

Since the SC Image Factory and SC Image objects fetch data over http, these must be called by asynchronous tasks. When the image is loaded, the "logImpression" method should be called to notify TripleLift that the advertisement has been served. If the user visits the clickthrough link from the app, the "logClickThrough" method should be called. Custom log events can also be triggered by calling "logEvent" with an event name parameter.
