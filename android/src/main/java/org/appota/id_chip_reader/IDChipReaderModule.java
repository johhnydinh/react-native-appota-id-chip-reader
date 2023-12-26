package org.appota.id_chip_reader;

import org.apache.commons.io.IOUtils;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import org.appota.id_chip_reader.DG13Reader;
import androidx.annotation.NonNull;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;

import net.sf.scuba.smartcards.CardService;
import net.sf.scuba.smartcards.CardServiceException;

import org.jmrtd.BACKey;
import org.jmrtd.BACKeySpec;
import org.jmrtd.PassportService;
import org.jmrtd.lds.SecurityInfo;
import org.jmrtd.lds.CardAccessFile;
import org.jmrtd.lds.PACEInfo;
import org.jmrtd.lds.SODFile;
import org.spongycastle.jce.provider.BouncyCastleProvider;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.io.InputStream;
import java.security.Security;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class IDChipReaderModule extends ReactContextBaseJavaModule implements LifecycleEventListener, ActivityEventListener {
  private static final String E_NOT_SUPPORTED = "E_NOT_SUPPORTED";
  private static final String E_NOT_ENABLED = "E_NOT_ENABLED";
  private static final String E_SCAN_CANCELED = "E_SCAN_CANCELED";
  private static final String E_SCAN_FAILED = "E_SCAN_FAILED";
  private static final String E_SCAN_FAILED_DISCONNECT = "E_SCAN_FAILED_DISCONNECT";
  private static final String E_ONE_REQ_AT_A_TIME = "E_ONE_REQ_AT_A_TIME";
  private static final String KEY_IS_SUPPORTED = "isSupported";
  private static final String PARAM_DOC_NUM = "documentNumber";
  private static final String PARAM_DOB = "dateOfBirth";
  private static final String PARAM_DOE = "dateOfExpiry";
  private static final String KEY_COM_FILE = "comFileEncoded";
  private static final String KEY_SOD_FILE = "sodFileEncoded";
  private static final String KEY_DSCRT_FILE = "dscFileEncoded";
  private static final String KEY_DG1_FILE = "dg1FileEncoded";
  private static final String KEY_DG2_FILE = "dg2FileEncoded";
  private static final String KEY_DG13_FILE = "dg13FileEncoded";
  private static final String KEY_DG14_FILE = "dg14FileEncoded";
  private static final String KEY_DG15_FILE = "dg15FileEncoded";
  private static final String KEY_ID_INFO = "idInfo";
  private static final String KEY_PROVINCE_INFO = "provinceInfo";
  private static final String KEY_FULLNAME_INFO = "fullNameInfo";
  private static final String KEY_DOB_INFO = "dateOfBirthInfo";
  private static final String KEY_GENDER_INFO = "genderInfo";
  private static final String KEY_NATIONALITY_INFO = "nationalityInfo";
  private static final String KEY_ETHNICITY_INFO = "ethnicityInfo";
  private static final String KEY_RELIGION_INFO = "religionInfo";
  private static final String KEY_POG_INFO = "placeOfOriginInfo";
  private static final String KEY_POR_INFO = "placeOfResidenceInfo";
  private static final String KEY_PERSONAL_IDENTIFICATION_INFO = "personalIdentificationInfo";
  private static final String KEY_DATEOFISSUE_INFO = "dateOfIssueInfo";
  private static final String KEY_DATEOFEXPIRY_INFO = "dateOfExpiryInfo";
  private static final String KEY_FATHERNAME_INFO = "fatherNameInfo";
  private static final String KEY_MOTHERNAME_INFO = "motherNameInfo";
  private static final String KEY_OLDID_INFO = "oldIdInfo";
  private static final String TAG = "IdChipReader";

  private BACKeySpec bacKey;
  private final ReactApplicationContext reactContext;
  private Promise scanPromise;
  private ReadableMap opts;

  public IDChipReaderModule(ReactApplicationContext reactContext) {
    super(reactContext);
    Security.insertProviderAt(new BouncyCastleProvider(), 1);

    reactContext.addLifecycleEventListener(this);
    reactContext.addActivityEventListener(this);

    this.reactContext = reactContext;
  }

  @Override
  public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
  }

  @Override
  public void onNewIntent(Intent intent) {
    if (scanPromise == null) return;
    if (!NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())) return;
    Tag tag = intent.getExtras().getParcelable(NfcAdapter.EXTRA_TAG);
    if (!Arrays.asList(tag.getTechList()).contains("android.nfc.tech.IsoDep")) return;
    this.bacKey = new BACKey(
      opts.getString(PARAM_DOC_NUM),
      opts.getString(PARAM_DOB),
      opts.getString(PARAM_DOE)
    );

    new ReadTask().execute(IsoDep.get(tag));
  }

  @NonNull
  @Override
  public String getName() {
    return "IDChipReader";
  }

  @Override
  public Map<String, Object> getConstants() {
    final Map<String, Object> constants = new HashMap<>();
    boolean hasNFC = reactContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_NFC);
    constants.put(KEY_IS_SUPPORTED, hasNFC);
    return constants;
  }

  @ReactMethod
  public void cancel(final Promise promise) {
    if (scanPromise != null) {
        scanPromise.reject(E_SCAN_CANCELED, "canceled");
    }

    resetState();

    NfcAdapter mNfcAdapter = NfcAdapter.getDefaultAdapter(reactContext);
    if (mNfcAdapter != null) {
        mNfcAdapter.disableForegroundDispatch(reactContext.getCurrentActivity());
    }

    promise.resolve(null);
  }

  @ReactMethod
  public void scan(final ReadableMap opts, final Promise promise) {
    NfcAdapter mNfcAdapter = NfcAdapter.getDefaultAdapter(reactContext);
    if (mNfcAdapter != null) {
      Activity activity = reactContext.getCurrentActivity();
      Intent intent = new Intent(activity, activity.getClass());
      intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
      PendingIntent pendingIntent = PendingIntent.getActivity(activity, 0, intent, PendingIntent.FLAG_MUTABLE);
      String[][] filter = new String[][] { new String[] { IsoDep.class.getName()  } };
      mNfcAdapter.enableForegroundDispatch(activity, pendingIntent, null, filter);
    } else { 
      promise.reject(E_NOT_SUPPORTED, "NFC chip reading not supported");
      return;
    }

    if (!mNfcAdapter.isEnabled()) {
      promise.reject(E_NOT_ENABLED, "NFC chip reading not enabled");
      return;
    }

    if (scanPromise != null) {
      promise.reject(E_ONE_REQ_AT_A_TIME, "Already running a scan");
      return;
    }

    this.opts = opts;
    this.scanPromise = promise;
  }

  private void resetState() {
    scanPromise = null;
    opts = null;
  }

  @Override
  public void onHostDestroy() {
    resetState();
  }

  @Override
  public void onHostResume() {
    NfcAdapter mNfcAdapter = NfcAdapter.getDefaultAdapter(reactContext);
    if (mNfcAdapter == null) return;

    Activity activity = reactContext.getCurrentActivity();
    Intent intent = new Intent(activity, activity.getClass());
    intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
    PendingIntent pendingIntent = PendingIntent.getActivity(activity, 0, intent, PendingIntent.FLAG_MUTABLE);//PendingIntent.FLAG_UPDATE_CURRENT);
    String[][] filter = new String[][] { new String[] { IsoDep.class.getName()  } };
    mNfcAdapter.enableForegroundDispatch(activity, pendingIntent, null, filter);
  }


  @Override
  public void onHostPause() {
    NfcAdapter mNfcAdapter = NfcAdapter.getDefaultAdapter(this.reactContext);
    if (mNfcAdapter == null) return;

    mNfcAdapter.disableForegroundDispatch(reactContext.getCurrentActivity());
  }

  public static String extractProvince(String text) {
    Pattern provincePattern = Pattern.compile("[^,\\\\s]+[^,]*$");

    Matcher provinceMatcher = provincePattern.matcher(text);
    if (provinceMatcher.find()) {
        return provinceMatcher.group().trim();
    } else {
      return null;
    }
  }

  @SuppressLint("StaticFieldLeak")
  public class ReadTask extends AsyncTask<IsoDep, Void, WritableMap> {
    @Override
    protected WritableMap doInBackground(IsoDep... params) {
      WritableMap id = null;
      try {
        IsoDep isoDep = params[0];
        isoDep.setTimeout(5 * 10000);
        CardService cardService = CardService.getInstance(isoDep);
        cardService.open();

        PassportService ps = new PassportService(
                cardService,
                PassportService.NORMAL_MAX_TRANCEIVE_LENGTH,
                PassportService.DEFAULT_MAX_BLOCKSIZE,
                false,
                false);
        ps.open();

        boolean isPACESucceeded = false;
        try {
          CardAccessFile cardAccessFile = new CardAccessFile(ps.getInputStream(PassportService.EF_CARD_ACCESS));
          Collection<SecurityInfo> securityInfos = cardAccessFile.getSecurityInfos();
          for (SecurityInfo securityInfo : securityInfos) {
            if (securityInfo instanceof PACEInfo) {
              ps.doPACE(
                      bacKey,
                      securityInfo.getObjectIdentifier(),
                      PACEInfo.toParameterSpec(((PACEInfo) securityInfo).getParameterId()),
                      null
              );
              isPACESucceeded = true;
            }
          }
        } catch (Exception e) {
          Log.w(TAG, e);
        }
        ps.sendSelectApplet(isPACESucceeded);

        if (!isPACESucceeded) {
          try {
            ps.getInputStream(PassportService.EF_COM).read();
          } catch (Exception e) {
            ps.doBAC(bacKey);
          }
        }
        
        DG13Reader dg13Info = new DG13Reader(Base64.getDecoder().decode(getDG13FileEncoded(ps)));

        id = Arguments.createMap();
        id.putString(KEY_COM_FILE, getComFileEncoded(ps));
        id.putString(KEY_SOD_FILE, getSodFileEncoded(ps));
        id.putString(KEY_DSCRT_FILE, getDsCertFileEncoded(ps));
        id.putString(KEY_DG1_FILE, getDG1FileEncoded(ps));
        id.putString(KEY_DG2_FILE, getDG2FileEncoded(ps));
        id.putString(KEY_DG13_FILE, getDG13FileEncoded(ps));
        id.putString(KEY_DG14_FILE, getDG14FileEncoded(ps));
        id.putString(KEY_DG15_FILE, getDG15FileEncoded(ps));
        id.putString(KEY_ID_INFO, dg13Info.eidNumber);
        id.putString(KEY_FULLNAME_INFO, dg13Info.fullName);
        id.putString(KEY_DOB_INFO, dg13Info.dateOfBirth);
        id.putString(KEY_GENDER_INFO, dg13Info.gender);
        id.putString(KEY_NATIONALITY_INFO, dg13Info.nationality);
        id.putString(KEY_ETHNICITY_INFO, dg13Info.ethnicity);
        id.putString(KEY_RELIGION_INFO, dg13Info.religion);
        id.putString(KEY_POG_INFO, dg13Info.placeOfOrigin);
        id.putString(KEY_POR_INFO, dg13Info.placeOfResidence);
        id.putString(KEY_PERSONAL_IDENTIFICATION_INFO, dg13Info.personalIdentification);
        id.putString(KEY_DATEOFISSUE_INFO, dg13Info.dateOfIssue);
        id.putString(KEY_DATEOFEXPIRY_INFO, dg13Info.dateOfExpiry);
        id.putString(KEY_FATHERNAME_INFO, dg13Info.fatherName);
        id.putString(KEY_MOTHERNAME_INFO, dg13Info.motherName);
        id.putString(KEY_OLDID_INFO, dg13Info.oldEidNumber);
        id.putString(KEY_PROVINCE_INFO, extractProvince(dg13Info.placeOfResidence));

      } catch (Exception e) {
        Log.w(TAG, "Could not read card");
      }
      return id;
    }

    private String convertDSCert(X509Certificate cert) {
      String b64cert = "";
        try {
            String pemCertificate = convertToPEMFormat(cert);
            b64cert = Base64.getEncoder().encodeToString(pemCertificate.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return b64cert;
    }

    private String convertToPEMFormat(X509Certificate certificate) throws CertificateEncodingException {
        StringBuilder pemFormat = new StringBuilder();
        pemFormat.append("-----BEGIN CERTIFICATE-----\n");
        pemFormat.append(Base64.getMimeEncoder(64, "\n".getBytes(StandardCharsets.UTF_8)).encodeToString(certificate.getEncoded()));
        pemFormat.append("\n-----END CERTIFICATE-----\n");
        return pemFormat.toString();
    }

    private String convertB64(InputStream ip) throws IOException {
      Object sourceBytes = IOUtils.toByteArray(ip);

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        return Base64.getEncoder().encodeToString((byte[]) sourceBytes);
      }
      return null;
    }

    private String getComFileEncoded(PassportService ps) throws CardServiceException, IOException {
      InputStream isComFile = null;
      try {
        isComFile = ps.getInputStream(PassportService.EF_COM);

        return convertB64(isComFile);
      } finally {
        if (isComFile != null) {
          isComFile.close();
          isComFile = null;
        }
      }
    }

    private String getSodFileEncoded(PassportService ps) throws CardServiceException, IOException {
      InputStream isSodFile = null;
      try {
        isSodFile = ps.getInputStream(PassportService.EF_SOD);

        return convertB64(isSodFile);
      } finally {
        if (isSodFile != null) {
          isSodFile.close();
          isSodFile = null;
        }
      }
    }

    private String getDsCertFileEncoded(PassportService ps) throws CardServiceException, IOException {
      InputStream isSodFile = null;
      try {
        isSodFile = ps.getInputStream(PassportService.EF_SOD);
        SODFile sodFile = new SODFile(isSodFile);
        String dsCert = convertDSCert(sodFile.getDocSigningCertificate());

        return dsCert;
      } finally {
        if (isSodFile != null) {
          isSodFile.close();
          isSodFile = null;
        }
      }
    }

    private String getDG1FileEncoded(PassportService ps) throws CardServiceException, IOException {
      InputStream isDG1 = null;
      try {
        isDG1 = ps.getInputStream(PassportService.EF_DG1);

        return convertB64(isDG1);
      } finally {
        if (isDG1 != null) {
          isDG1.close();
          isDG1 = null;
        }
      }
    }

    private String getDG2FileEncoded(PassportService ps) throws CardServiceException, IOException {
      InputStream isDG2 = null;
      try {
        isDG2 = ps.getInputStream(PassportService.EF_DG2);

        return convertB64(isDG2);
      } finally {
        if (isDG2 != null) {
          isDG2.close();
          isDG2 = null;
        }
      }
    }

    private String getDG13FileEncoded(PassportService ps) throws CardServiceException, IOException {
      InputStream isDG13 = null;
      try {
        isDG13 = ps.getInputStream(PassportService.EF_DG13);

        return convertB64(isDG13);
      } finally {
        if (isDG13 != null) {
          isDG13.close();
          isDG13 = null;
        }
      }
    }

    private String getDG14FileEncoded(PassportService ps) throws CardServiceException, IOException {
      InputStream isDG14 = null;
      try {
        isDG14 = ps.getInputStream(PassportService.EF_DG14);

        return convertB64(isDG14);
      } finally {
        if (isDG14 != null) {
          isDG14.close();
          isDG14 = null;
        }
      }
    }

    private String getDG15FileEncoded(PassportService ps) throws CardServiceException, IOException {
      InputStream isDG15 = null;
      try {
        isDG15 = ps.getInputStream(PassportService.EF_DG15);

        return convertB64(isDG15);
      } finally {
        if (isDG15 != null) {
          isDG15.close();
          isDG15 = null;
        }
      }
    }

    @Override
    protected void onPostExecute(WritableMap id) {
        if (scanPromise == null) return;

        // Stop NFC
        NfcAdapter mNfcAdapter = NfcAdapter.getDefaultAdapter(reactContext);
        if (mNfcAdapter != null) {
            mNfcAdapter.disableForegroundDispatch(reactContext.getCurrentActivity());
        }

        // Resolve the promise
        scanPromise.resolve(id);
        resetState();
    }
  }
}
