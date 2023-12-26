# react-native-appota-id-chip-reader
## Developed by Appota SRD
![alt text](https://i.ibb.co/XY1S796/SRD-Logo.png)

## Getting started

```sh
$ npm install react-native-appota-id-chip-reader --save
$ react-native link react-native-appota-id-chip-reader
```

For Android:
In `AndroidManifest.xml` add:

```xml
<uses-feature android:name="android.hardware.nfc" android:required="true" />
```

For IOS:
1. In `Info.plist` add:

```xml
<key>NFCReaderUsageDescription</key>
<string>NFC ID Chip Reader</string>
<key>com.apple.developer.associated-domains</key>
<array>
    <string>applinks:org.appota.id</string>
</array>
<key>com.apple.developer.nfc.readersession.formats</key>
<array>
    <string>NDEF</string>
    <string>TAG</string>
</array>
<key>com.apple.developer.nfc.readersession.iso7816.select-identifiers</key>
<array>
  <string>A0000002471001</string>
  <string>A0000002472001</string>
  <string>00000000000000</string>
</array>
```

2. Go to Targets -> Signing & Capabilities -> +Capability -> Near Field Communication Tag Reading

## Usage
```js
import IDChipReader from 'react-native-appota-id-chip-reader'
// { scan, cancel }
    async function scan() {
        const {
          comFileEncoded,
          sodFileEncoded,
          dscFileEncoded,
          dg1FileEncoded,
          dg2FileEncoded,
          dg13FileEncoded,
          dg14FileEncoded,
          dg15FileEncoded,
          idInfo,
          provinceInfo,
          fullNameInfo,
          dateOfBirthInfo,
          genderInfo,
          nationalityInfo,
          ethnicityInfo,
          religionInfo,
          placeOfOriginInfo,
          placeOfResidenceInfo,
          personalIdentificationInfo,
          dateOfIssueInfo,
          dateOfExpiryInfo,
          fatherNameInfo,
          motherNameInfo,
          oldIdInfo
        } = await IDChipReader.scan({
            documentNumber: '199004922',
            dateOfBirth: '990605',
            dateOfExpiry: '390605'
      })
    }
```
