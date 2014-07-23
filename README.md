## Sick Videos

### YouTube client for Android

This is a full featured YouTube client for Android that lets the user view either one or a custom list of YouTube channels. [https://code.google.com/apis/console](https://code.google.com/apis/console).  The technews build variant allow you to access a users YouTube subcription list, so set up your own OAuth 2 client id on that page too, but it's not required to build and run the app.

This project is setup for Android Studio.

You will need a valid YouTube developer id for "Public API access" to build this app.  It's free and easy to obtain. 

There are many build variants for single channeled apps, but you probably would be most interested in the 'technews' build variant as that enables you to customize the list of YouTube channels.  The default set of channels is tech related news channels, but these can be edited to your own favorites.  Each build variant contains a subfolder with only style and string differences.  The code is 100% shared for all variants.

The single channel apps were an experiment in UI. Currnently you can open up the Android YouTube app and click on a subscribed channel in the navigation drawer to view a favorite channel.  That's great, but since I already use the app launcher to launch content I'm interested in, why not make that also launch YouTube channels?  Why does the user need two ways of navigating content when the application launcher already serves this purpose.  

Posting these apps to the Google Play Store got me banned for life, so be aware of this if you plan on posting your versions to the store.  Google should not ban developers for innovating, but they do.

Feel free to build your own variant of this code and share them with your friends. 

I did some work months ago on Chromecast support, so it's 99% there.  Fixing this would be great.  Last I checked YouTube clients were not supported for Chromecast, so please confirm this has been fixed before working on this.

This app uses many popular third party libraries, so it contains many real world working examples of using these libraries.

I welcome your pull requests!

Developed by: [Distant Future Technologies](http://distantfutu.re/).


