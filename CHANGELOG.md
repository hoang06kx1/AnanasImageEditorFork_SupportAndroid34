# Changelog
All notable changes to this project will be documented in this file.

# Released

## [AnanasImageEditorFork 1.1.0]
- Translated the strings to BG, DE, IT, PT, RU and corrected some of the EN strings

## [AnanasImageEditorFork 1.0.0]
- Increased targetSdk to 33
- targetSdk 33: Permission READ_MEDIA_IMAGES used instead of READ_EXTERNAL_STORAGE, which is deprecated 
- targetSdk 33: Permission WRITE_EXTERNAL_STORAGE not used becaused deprecated
- Updated the versions of the dependencies
- Removed the kotlin stdlib dependency
- Replaced the Cropper library with the currently supported one

## [1.2.6]
- Fixed blank image at first time load
- Added new picker for gallery and camera photos
- Fixed crashes on Android 10 <= devices 

## [1.2.5]
- Added Arabic and French locales
- Fixed image orientation issue while loading bitmap
- Fixed demo app crash on Android Q and above while loading images 

## [1.2.4]
### Enhancement
- Added toggle for SupportActionBar

## [1.2.1]
### Bugfix
- Added auto-scale to AddTextFragment

## [1.2.0]
### Enhancement 
- Improved Paint feature UI
- Fixed bug to reset on back to main options in Saturation, Brightness and Sticker Fragment
- Improve Sticker feature UI
- Updated Beauty feature seekbar hex.

## [1.1.3]
### Changes
- Simplified rotate feature UX

## [1.1.2]
### Changes
- Migrated to AndroidX
- Fixed crop slider stroke
- Added toast message to MainActivity
- Replace UniversalImageLoader with Glide
- Improved CropFragment with Cropper library
- Fix sticker scale bug

## [1.1.1]
### Bugfix
- Fixed memory leaks from AsyncTask by replacing with RxJava

## [1.1.0]
### Bugfix
- Improved Add Text fragment
- BeautyFragment now uses RxJava instead of AsyncTask.
- IntentBuilder Pattern to customized the library on demand.