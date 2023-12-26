package org.appota.id_chip_reader;

import java.util.ArrayList;
import java.util.List;

public class DG13Reader {

    private static final byte IDX_EID = 1;
    public static final byte[] PREFIX_EID = {48, 17, 2, 1, 1, 19, 12};

    public static final byte IDX_FULLNAME = 2;
    public static final byte[] PREFIX_FULLNAME = {48, 28, 2, 1, 2, 12, 23};

    public static final byte IDX_DOB = 3;
    public static final byte[] PREFIX_DOB = {48, 15, 2, 1, 3, 19, 10};

    public static final byte IDX_GENDER = 4;
    public static final byte[] PREFIX_GENDER = {48, 8, 2, 1, 4, 12, 3};

    public static final byte IDX_NATIONALITY = 5;
    public static final byte[] PREFIX_NATIONALITY = {48, 15, 2, 1, 5, 12, 10};

    public static final byte IDX_ETHNICITY = 6;
    public static final byte[] PREFIX_ETHNICITY = {48, 9, 2, 1, 6, 12, 4};

    public static final byte IDX_RELIGION = 7;
    public static final byte[] PREFIX_RELIGION = {48, 11, 2, 1, 7, 12, 6};

    public static final byte IDX_POG = 8;
    public static final byte[] PREFIX_POG = {48, 38, 2, 1, 8, 12, 33};

    public static final byte IDX_POR = 9;
    public static final byte[] PREFIX_POR = {48, 61, 2, 1, 9, 12, 56};

    public static final byte IDX_PERSONAL_IDENTIFICATION = 10;
    public static final byte[] PREFIX_PERSONAL_IDENTIFICATION = {48, 40, 2, 1, 10, 12, 35};

    public static final byte IDX_DATEOFISSUE = 11;
    public static final byte[] PREFIX_DATEOFISSUE = {48, 15, 2, 1, 11, 19, 10};

    public static final byte IDX_DATEOFEXPIRY = 12;
    public static final byte[] PREFIX_DATEOFEXPIRY = {48, 15, 2, 1, 12, 12, 10};

    public static final byte IDX_FAMILY = 13;
    public static final byte[] PREFIX_FAMILY = {48, 54, 2, 1, 13};
    public static final byte[] PREFIX_FATHERNAME = {48, 25, 12, 23};
    public static final byte[] PREFIX_MOTHERNAME = {48, 22, 12, 20};

    public static final byte IDX_CARDINFO = 14;
    public static final byte[] PREFIX_CARDINFO = {48, 3, 2, 1, 14};
    public static final byte IDX_OLDEID = 15;
    public static final byte[] PREFIX_OLDEID = {48, 14, 2, 1, 15, 19, 9};
    public static final byte IDX_CARDUNK = 16;
    public static final byte[] PREFIX_UNK = {48, 21, 2, 1, 16, 19, 16};

    public String eidNumber;
    public String fullName;
    public String dateOfBirth;
    public String gender;
    public String nationality;
    public String ethnicity;
    public String religion;
    public String placeOfOrigin;
    public String placeOfResidence;
    public String personalIdentification;
    public String dateOfIssue;
    public String dateOfExpiry;
    public String fatherName;
    public String motherName;
    public String oldEidNumber;
    public String unkIdNumber;
    public List<String> unkInfo = new ArrayList<>();

    public DG13Reader(byte[] data) throws Exception {
        parse(data);
    }

    void parse(byte[] buf) throws Exception {
        List<Integer> separatorPositions = new ArrayList<>();
        int segmentIdx = 1;

        for (int i = 0; i < buf.length - 5; i++) {
            byte[] c5 = {buf[i], buf[i + 1], buf[i + 2], buf[i + 3], buf[i + 4]};

            if (c5[0] == 48 && c5[2] == 2 && c5[3] == 1 && c5[4] == segmentIdx) {
                segmentIdx++; // increment next segment

                separatorPositions.add(i);
            }
        }
        separatorPositions.add(buf.length);

        for (int i = 0; i < separatorPositions.size() - 1; i++) {
            int start = separatorPositions.get(i);
            int end = separatorPositions.get(i + 1);
            byte[] subset = new byte[end - start];
            System.arraycopy(buf, start, subset, 0, subset.length);

            // Potential empty group here
            if (subset.length < 5) {
                continue;
            }

            switch (subset[4]) {
                case IDX_EID:
                    eidNumber = subset.length >= PREFIX_EID.length
                            ? new String(subset, PREFIX_EID.length, subset.length - PREFIX_EID.length)
                            : "";
                    break;
                case IDX_FULLNAME:
                    fullName = subset.length >= PREFIX_FULLNAME.length
                            ? new String(subset, PREFIX_FULLNAME.length, subset.length - PREFIX_FULLNAME.length)
                            : "";
                    break;
                case IDX_DOB:
                    dateOfBirth = subset.length >= PREFIX_DOB.length
                            ? new String(subset, PREFIX_DOB.length, subset.length - PREFIX_DOB.length)
                            : "";
                    break;
                case IDX_GENDER:
                    gender = subset.length >= PREFIX_GENDER.length
                            ? new String(subset, PREFIX_GENDER.length, subset.length - PREFIX_GENDER.length)
                            : "";
                    break;
                case IDX_NATIONALITY:
                    nationality = subset.length >= PREFIX_NATIONALITY.length
                            ? new String(subset, PREFIX_NATIONALITY.length, subset.length - PREFIX_NATIONALITY.length)
                            : "";
                    break;
                case IDX_ETHNICITY:
                    ethnicity = subset.length >= PREFIX_ETHNICITY.length
                            ? new String(subset, PREFIX_ETHNICITY.length, subset.length - PREFIX_ETHNICITY.length)
                            : "";
                    break;
                case IDX_RELIGION:
                    religion = subset.length >= PREFIX_RELIGION.length
                            ? new String(subset, PREFIX_RELIGION.length, subset.length - PREFIX_RELIGION.length)
                            : "";
                    break;
                case IDX_POG:
                    placeOfOrigin = subset.length >= PREFIX_POG.length
                            ? new String(subset, PREFIX_POG.length, subset.length - PREFIX_POG.length)
                            : "";
                    break;
                case IDX_POR:
                    placeOfResidence = subset.length >= PREFIX_POR.length
                            ? new String(subset, PREFIX_POR.length, subset.length - PREFIX_POR.length)
                            : "";
                    break;
                case IDX_PERSONAL_IDENTIFICATION:
                    personalIdentification = subset.length >= PREFIX_PERSONAL_IDENTIFICATION.length
                            ? new String(subset, PREFIX_PERSONAL_IDENTIFICATION.length, subset.length - PREFIX_PERSONAL_IDENTIFICATION.length)
                            : "";
                    break;
                case IDX_DATEOFISSUE:
                    dateOfIssue = subset.length >= PREFIX_DATEOFISSUE.length
                            ? new String(subset, PREFIX_DATEOFISSUE.length, subset.length - PREFIX_DATEOFISSUE.length)
                            : "";
                    break;
                case IDX_DATEOFEXPIRY:
                    dateOfExpiry = subset.length >= PREFIX_DATEOFEXPIRY.length
                            ? new String(subset, PREFIX_DATEOFEXPIRY.length, subset.length - PREFIX_DATEOFEXPIRY.length)
                            : "";
                    break;
                case IDX_FAMILY:
                    List<Integer> seps = new ArrayList<>();
                    for (int j = PREFIX_FAMILY.length; j < subset.length - 2; j++) {
                        if (subset[j] == 48 && subset[j + 2] == 12) {
                            seps.add(j);
                        }
                    }
                    if (seps.size() != 2) {
                        System.out.println("FAMILY: Bad format");
                        break;
                    }
                    fatherName = new String(subset, seps.get(0) + PREFIX_FATHERNAME.length, seps.get(1) - (seps.get(0) + PREFIX_FATHERNAME.length));
                    motherName = new String(subset, seps.get(1) + PREFIX_MOTHERNAME.length, subset.length - (seps.get(1) + PREFIX_MOTHERNAME.length));
                    break;
                case IDX_CARDINFO:
                    // empty data
                    // data.setExpiryDate(new String(Arrays.copyOfRange(subset, PREFIX_EXPIRYDATE.length, subset.length)))
                    break;
                case IDX_OLDEID:
                    oldEidNumber = subset.length >= PREFIX_OLDEID.length
                            ? new String(subset, PREFIX_OLDEID.length, subset.length - PREFIX_OLDEID.length)
                            : "";
                    break;
                case IDX_CARDUNK:
                    unkIdNumber = subset.length >= IDX_CARDUNK
                            ? new String(subset, PREFIX_UNK.length, subset.length - PREFIX_UNK.length)
                            : "";
                    break;
                default:
                    unkInfo.add(new String(subset));
            }
        }
    }
}