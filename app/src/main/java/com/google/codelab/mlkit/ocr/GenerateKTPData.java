package com.google.codelab.mlkit.ocr;

import android.text.TextUtils;

import com.google.mlkit.vision.text.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GenerateKTPData {

    public interface Listener {
        void finishScan();
    }

    private List<String> resultScan;
    private Text text;

    // Helper
    private final IdentifierChecker identifierChecker = new IdentifierChecker();

    private final OCRValidator ocrValidator = new OCRValidator();

    // Header
    private String provinsi = "";
    private String kabupatenKota = "";

    // Store data here
    private final KTPData nik = new KTPData();
    private final KTPData nama = new KTPData();
    private final KTPData tempatLahir = new KTPData();
    private final KTPData tanggalLahir = new KTPData();
    private final KTPData jenisKelamin = new KTPData();
    private final KTPData alamat = new KTPData();
    private final KTPData rtRw = new KTPData();
    private final KTPData kelDesa = new KTPData();
    private final KTPData kecamatan = new KTPData();
    private final KTPData agama = new KTPData();
    private final KTPData statusPerkawinan = new KTPData();
    private final KTPData pekerjaan = new KTPData();
    private final KTPData kewarganegaraan = new KTPData();
    private final KTPData berlakuHingga = new KTPData();
    private final KTPData golonganDarah = new KTPData();

    // Status found
    private boolean isNikFound = false;
    private boolean isNamaFound = false;
    private boolean isTempatLahirFound = false;
    private boolean isTanggalLahirFound = false;
    private boolean isJenisKelaminFound = false;
    private boolean isAlamatFound = false;
    private boolean isRtRwFound = false;
    private boolean isKelDesaFound = false;
    private boolean isKecamatanFound = false;
    private boolean isAgamaFound = false;
    private boolean isStatusPerkawinanFound = false;
    private boolean isGolonganDarahFound = false;

    List<String> identifiers = new ArrayList<>();
    private int lastIndexIdentifier = 0;
    private int lastIndexValue = 0;

    public GenerateKTPData(List<String> resultScan, GenerateKTPData.Listener listener) {
        System.out.println(Arrays.toString(resultScan.toArray()));
        this.resultScan = resultScan;
        mappingData(listener);
    }

    public GenerateKTPData(Text text, GenerateKTPData.Listener listener) {
        this.text = text;
        mappingDataWithText(listener);
    }

    private boolean isNotIdentifier(String data){
        boolean result = data.equalsIgnoreCase("nik") || data.equals("Nama")
                || identifierChecker.isIdentifierTempatTanggalLahir(data)
                || identifierChecker.isIdentifierJenisKelamin(data) || identifierChecker.isIdentifierAlamat(data)
                || identifierChecker.isIdentifierRtRw(data) || identifierChecker.isIdentifierKelDesa(data)
                || identifierChecker.isIdentifierKecamatan(data) || identifierChecker.isIdentifierAgama(data)
                || identifierChecker.isIdentifierStatusPerkawinan(data) || identifierChecker.isIdentifierGolDarah(data)
                || data.toLowerCase().contains("provinsi") || identifierChecker.isIdentifierKabKota(data)
                || identifierChecker.isNotIdentifierPekerjaan(data) || identifierChecker.isNotIdentifierBerlakuHingga(data)
                || identifierChecker.isNotIdentifierKewarganegaraan(data);
        return !result;
    }

    private void mappingDataWithText(GenerateKTPData.Listener listener){
        for (Text.TextBlock block : text.getTextBlocks()){
            for (Text.Line line : block.getLines()){
                String data = line.getText();
                if (getNIKData(data)) continue;
                if (getNamaData(data)) continue;
                if (getTempatLahirData(data)) continue;
                if (getTanggalLahirData(data)) continue;
                if (getJenisKelaminData(data)) continue;
                if (getAlamatData(data)) continue;
                if (getRtRwData(data)) continue;
                if (getKelDesaData(data)) continue;
                if (getKecamatanData(data)) continue;
                if (getAgamaData(data)) continue;
                if (getStatusPerkawinanData(data)) continue;
                if (getProvinsiData(data)) continue;
                if (getKabupatenKotaData(data)) continue;
                getGolonganDarahData(data);
            }
        }
        validateData(listener);
    }

    private void mappingData(GenerateKTPData.Listener listener){
        // Can be converted into List<Text.Line> instead of List<String> to reduce looping
        for (String data : resultScan){
            if (getNIKData(data)) continue;
            if (getNamaData(data)) continue;
            if (getTempatLahirData(data)) continue;
            if (getTanggalLahirData(data)) continue;
            if (getJenisKelaminData(data)) continue;
            if (getAlamatData(data)) continue;
            if (getRtRwData(data)) continue;
            if (getKelDesaData(data)) continue;
            if (getKecamatanData(data)) continue;
            if (getAgamaData(data)) continue;
            if (getStatusPerkawinanData(data)) continue;
            if (getProvinsiData(data)) continue;
            if (getKabupatenKotaData(data)) continue;
            getGolonganDarahData(data);
        }
        validateData(listener);
    }

    private void validateData(final GenerateKTPData.Listener listener){
        ocrValidator.validateData(
                nik.getValue(), tanggalLahir.getValue(), rtRw.getValue(), golonganDarah.getValue(),
                jenisKelamin.getValue(), new OCRValidator.ValueSetter() {
                    @Override
                    public void setNikValue(String data) {
                        nik.setValue(data);
                    }

                    @Override
                    public void setTanggalLahirValue(String data) {
                        tanggalLahir.setValue(data);
                    }

                    @Override
                    public void setRtRwValue(String data) {
                        rtRw.setValue(data);
                    }

                    @Override
                    public void setGolonganDarahValue(String data) {
                        golonganDarah.setValue(data);
                    }

                    @Override
                    public void setJenisKelaminValue(String data) {
                        jenisKelamin.setValue(data);
                    }

                    @Override
                    public void finishAll() {
                        printResult();
                        listener.finishScan();
                    }
                });
    }

    private void printResult(){
        System.out.println("NIK "+nik.getValue());
        System.out.println("NAMA "+nama.getValue());
        System.out.println("TEMPAT LAHIR "+tempatLahir.getValue());
        System.out.println("TANGGAL LAHIR "+tanggalLahir.getValue());
        System.out.println("JENIS KELAMIN "+jenisKelamin.getValue());
        System.out.println("ALAMAT "+alamat.getValue());
        System.out.println("RT/RW "+rtRw.getValue());
        System.out.println("KEL/DESA "+kelDesa.getValue());
        System.out.println("KECAMATAN "+kecamatan.getValue());
        System.out.println("AGAMA "+agama.getValue());
        System.out.println("STATUS PERKAWINAN "+statusPerkawinan.getValue());
        System.out.println("PROVINSI "+provinsi);
        System.out.println("KABUPATEN/KOTA "+kabupatenKota);
        System.out.println("GOLONGAN DARAH "+golonganDarah.getValue());
        System.out.println(Arrays.toString(identifiers.toArray()));

    }

    private boolean checkIdentifier(String identifier){
        switch (identifier){
            case "nik": return isNikFound;
            case "nama": return isNamaFound;
            case "ttl": return isTempatLahirFound;
            case "alamat": return isAlamatFound;
            case "rtrw": return isRtRwFound;
            case "keldesa": return isKelDesaFound;
            case "kec": return isKecamatanFound;
            case "agama": return isAgamaFound;
            case "sp": return isStatusPerkawinanFound;
            case "gd": return isGolonganDarahFound;
        }
        return false;
    }

    private boolean isPreviousAndNextFound(String identifier){
        int index = identifiers.indexOf(identifier);
        boolean prev = false, next = false;
        int lastIndex = identifiers.size() - 1;
        if (index < 0) return false;
        if (index > 0){
            String idtf = identifiers.get(index - 1);
            prev = checkIdentifier(idtf);
        }
        if (index < lastIndex){
            String idtf = identifiers.get(index + 1);
            next = checkIdentifier(idtf);
        }
        return prev || next;
    }

    // Scpecial field : Jenis Kelamin, Golongan Darah, Agama
    private boolean exceptionForSpecialField(String data){
        data = data.toLowerCase();
        boolean result = ocrValidator.isValidAgama(data) || ocrValidator.isValidRtRw(data)
                || ocrValidator.isValidJenisKelamin(data) || ocrValidator.isValidGolDarah(data)
                || data.endsWith("kawin") || data.equals("wni");
        return !result;
    }

    // Check if data contains any number

    private boolean getNIKData(String data){
        if (isNikFound) return false; // skip process if nik is already found
        boolean isNumberExist = ocrValidator.isNumberExist(data);
        // check if data is equal to nik with case ignored
        if (data.equalsIgnoreCase("nik") && nik.getIndex() < 0) {
            nik.setIndex(lastIndexIdentifier);
            lastIndexIdentifier++;
            identifiers.add("nik");
            return true;
        }
        // check if first character is ':' and if length of character is 17 with deleted space if exist
        if (data.toCharArray()[0] == ':' && data.replace(" ", "").length() == 17){
            // check if data contains any number
            if (isNumberExist){
                // set a value to nik, change nik found to true and update last index for value
                nik.setValue(data.replace(":", "").replace(" ",""));
                isNikFound = true;
                lastIndexValue++;
                return true;
            }
        }
        // check if data with space removed and no ':' found, length is 16 and contains any number
        if (data.replace(" ", "").length() == 16 && isNumberExist){
            nik.setValue(data.replace(" ", ""));
            isNikFound = true;
            lastIndexValue++;
            return true;
        }
        return false;
    }

    private boolean getNamaData(String data){
        if (isNamaFound) return false; // skip process if nama is already found
        if (data.equals("Nama") && nama.getIndex() < 0){
            nama.setIndex(lastIndexIdentifier);
            lastIndexIdentifier++;
            identifiers.add("nama");
            return true;
        }
        if (data.toCharArray()[0] == ':' && lastIndexValue == nama.getIndex() && exceptionForSpecialField(data)){
            nama.setValue(data.replace(":",""));
            lastIndexValue++;
            isNamaFound = true;
            return true;
        }
        if (isNotIdentifier(data) && exceptionForSpecialField(data) && !ocrValidator.isPossibleAlamat(data)
                && !ocrValidator.isPossibleDate(data)){
            if (identifiers.indexOf("nama") == lastIndexValue && !data.contains(":")){
                nama.setValue(data);
                lastIndexValue++;
                isNamaFound = true;
                return true;
            } else if (isPreviousAndNextFound("nama")){
                nama.setValue(data);
                lastIndexValue++;
                isNamaFound = true;
                return true;
            }
        }
        return false;
    }

    private boolean getTempatLahirData(String data){
        if (data.length() < 11) return false;
        boolean isTempatTglLahir = identifierChecker.isIdentifierTempatTanggalLahir(data);
        if (isTempatLahirFound) return false;
        if (isTempatTglLahir && !data.contains(":") && tempatLahir.getIndex() < 0 && data.length() <= 16){
            tempatLahir.setIndex(lastIndexIdentifier);
            tanggalLahir.setIndex(lastIndexIdentifier);
            lastIndexIdentifier++;
            identifiers.add("ttl");
            return true;
        }
        if (isTempatTglLahir && data.contains(":")){
            String ttlLengkap = data.split(":")[1];
            if (data.contains("-") && data.contains(",")){
                String[] ttl = ttlLengkap.split(",");
                tempatLahir.setValue(ttl[0]);
                tanggalLahir.setValue(ttl[1]);
                isTanggalLahirFound = true;
            } else {
                tempatLahir.setValue(ttlLengkap);
            }
            isTempatLahirFound = true;
            return true;
        }
        if (data.toCharArray()[0] == ':' && lastIndexValue == tempatLahir.getIndex() && exceptionForSpecialField(data)){
            if (data.contains(",")){
                String[] ttl = data.replace(":", "").split(",");
                tempatLahir.setValue(ttl[0]);
                tanggalLahir.setValue(ttl[1]);
                isTanggalLahirFound = true;
            } else {
                tempatLahir.setValue(data.replace(":", ""));
            }
            lastIndexValue++;
            isTempatLahirFound = true;
            return true;
        }
        if (!data.contains(":") && data.toLowerCase().contains("lahir") && data.toLowerCase().split("lahir").length > 1){
            String[] ttl = data.toLowerCase().split("lahir");
            if (ttl[1].contains("-") && ttl[1].contains(",")){
                String[] ttlNow = ttl[1].split(",");
                tempatLahir.setValue(ttlNow[0].toUpperCase());
                tanggalLahir.setValue(ttlNow[1].replace(" ",""));
                isTanggalLahirFound = true;
            } else {
                tempatLahir.setValue(ttl[1].toUpperCase());
            }
            isTempatLahirFound = true;
            return true;
        }
        if (data.contains(",") && data.contains("-")) {
            String[] ttl = data.replace(":","").split(",");
            tempatLahir.setValue(ttl[0]);
            tanggalLahir.setValue(ttl[1].replace(" ",""));
            isTempatLahirFound = true;
            isTanggalLahirFound = true;
            lastIndexValue++;
            return true;
        }
        if (data.toCharArray()[0] != ':' && data.contains(":") && data.contains(",") && isTempatTglLahir){
            String[] ttl = data.split(":")[0].split(",");
            tempatLahir.setValue(ttl[0]);
            tanggalLahir.setValue(ttl[1]);
            isTempatLahirFound = true;
            isTanggalLahirFound = true;
            return true;
        }
        return false;
    }

    private boolean getTanggalLahirData(String data){
        if (isTanggalLahirFound) return false;
        if (lastIndexValue - 1 == tanggalLahir.getIndex() && data.split("-").length == 3 && isTempatLahirFound){
            tanggalLahir.setValue(data);
            isTanggalLahirFound = true;
            return true;
        }
        if (!data.contains(":") && data.contains("-") && ocrValidator.isNumberExist(data) && data.split("-").length == 3){
            tanggalLahir.setValue(data);
            isTanggalLahirFound = true;
            return true;
        }
        return false;
    }

    private boolean getJenisKelaminData(String data){
        if (isJenisKelaminFound) return false;
        if (data.length() < 3) return false;
        boolean isIdentifier = identifierChecker.isIdentifierJenisKelamin(data);
        boolean isValue = ocrValidator.isValidJenisKelamin(data);
        if (data.toCharArray()[0] != ':' && isIdentifier && jenisKelamin.getIndex() < 0){
            jenisKelamin.setIndex(lastIndexIdentifier);
            lastIndexIdentifier++;
            identifiers.add("jk");
            return true;
        }
        if (data.toCharArray()[0] == ':' && lastIndexValue == jenisKelamin.getIndex() && isValue){
            jenisKelamin.setValue(data.replace(":", ""));
            lastIndexValue++;
            isJenisKelaminFound = true;
            return true;
        }
        if (isValue){
            jenisKelamin.setValue(data);
            if (jenisKelamin.getIndex() > -1) lastIndexValue++;
            isJenisKelaminFound = true;
            return true;
        }
        return false;
    }

    private boolean getAlamatData(String data){
        if (isAlamatFound) return false;
        boolean isIdentifier = identifierChecker.isIdentifierAlamat(data);
        if (data.toCharArray()[0] != ':' && isIdentifier && alamat.getIndex() < 0){
            alamat.setIndex(lastIndexIdentifier);
            lastIndexIdentifier++;
            identifiers.add("alamat");
            return true;
        }
        if (data.toCharArray()[0] == ':' && lastIndexValue == alamat.getIndex() && exceptionForSpecialField(data)){
            alamat.setValue(data.replace(":", ""));
            lastIndexValue++;
            isAlamatFound = true;
            return true;
        }
        if (isNotIdentifier(data) && exceptionForSpecialField(data)) {
            String dt = data.contains(":") ? data.replace(":","") : data;
            if (ocrValidator.isPossibleAlamat(data)){
                alamat.setValue(dt);
                lastIndexValue++;
                isAlamatFound = true;
                return true;
            } else if (lastIndexValue == alamat.getIndex()){
                alamat.setValue(dt);
                lastIndexValue++;
                isAlamatFound = true;
                return true;
            } else if (isPreviousAndNextFound("alamat")){
                alamat.setValue(dt);
                lastIndexValue++;
                isAlamatFound = true;
                return true;
            }
        }
        return false;
    }

    private boolean getRtRwData(String data){
        if (isRtRwFound) return false;
        boolean isIdentifier = identifierChecker.isIdentifierRtRw(data);
        if (isIdentifier && !data.contains(":") && rtRw.getIndex() < 0){
            rtRw.setIndex(lastIndexIdentifier);
            lastIndexIdentifier++;
            identifiers.add("rtrw");
            return true;
        }
        if (data.toCharArray()[0] == ':' && lastIndexValue == rtRw.getIndex()){
            rtRw.setValue(data.replace(":", ""));
            lastIndexValue++;
            isRtRwFound = true;
            return true;
        }
        if (ocrValidator.isValidRtRw(data)){
            rtRw.setValue(data);
            if (rtRw.getIndex() > -1) lastIndexValue++;
            isRtRwFound = true;
            return true;
        }
        return false;
    }

    private boolean getKelDesaData(String data){
        if (isKelDesaFound) return false;
        if (data.length() < 4) return false;
        boolean isIdentifier = identifierChecker.isIdentifierKelDesa(data);
        if (isIdentifier && kelDesa.getIndex() < 0){
            kelDesa.setIndex(lastIndexIdentifier);
            lastIndexIdentifier++;
            identifiers.add("kd");
            return true;
        }
        if (isNotIdentifier(data) && exceptionForSpecialField(data)
                && !ocrValidator.isPossibleDate(data) && !ocrValidator.isPossibleAlamat(data)){
            String dt = data.contains(":") ? data.replace(":","") : data;
            if (lastIndexValue == kelDesa.getIndex()){
                kelDesa.setValue(dt);
                lastIndexValue++;
                isKelDesaFound = true;
                return true;
            } else if (isPreviousAndNextFound("kd")){
                kelDesa.setValue(data.replace(":", ""));
                lastIndexValue++;
                isKelDesaFound = true;
                return true;
            }
        }
//        if (data.toCharArray()[0] == ':' && lastIndexValue == kelDesa.getIndex() && exceptionForSpecialField(data)){
//            kelDesa.setValue(data.replace(":", ""));
//            lastIndexValue++;
//            isKelDesaFound = true;
//            return true;
//        }
        return false;
    }

    private boolean getKecamatanData(String data){
        if (isKecamatanFound) return false;
        boolean isIdentifier = identifierChecker.isIdentifierKecamatan(data);
        if (isIdentifier && kecamatan.getIndex() < 0 && !data.contains(":")){
            kecamatan.setIndex(lastIndexIdentifier);
            lastIndexIdentifier++;
            identifiers.add("kec");
            return true;
        }
        if (data.toCharArray()[0] == ':' && lastIndexValue == kecamatan.getIndex() && exceptionForSpecialField(data)){
            kecamatan.setValue(data.replace(":",""));
            lastIndexValue++;
            isKecamatanFound = true;
            return true;
        }
        if (isNotIdentifier(data) && exceptionForSpecialField(data) && !ocrValidator.isPossibleDate(data)
                && !ocrValidator.isPossibleAlamat(data)){
            String dt = data.contains(":") ? data.replace(":","") : data;
            if (lastIndexValue == kecamatan.getIndex()){
                kecamatan.setValue(dt);
                lastIndexValue++;
                isKecamatanFound = true;
                return true;
            } else if (isPreviousAndNextFound("kec")){
                kecamatan.setValue(data.replace(":", ""));
                lastIndexValue++;
                isKecamatanFound = true;
                return true;
            }
        }
        return false;
    }

    private boolean getAgamaData(String data){
        if (isAgamaFound) return false;
        if (data.length() < 2) return false;
        boolean isIdentifier = identifierChecker.isIdentifierAgama(data);
        if (isIdentifier && agama.getIndex() < 0){
            agama.setIndex(lastIndexIdentifier);
            lastIndexIdentifier++;
            identifiers.add("agama");
            return true;
        }
        boolean isNormalReligion = ocrValidator.isValidAgama(data);
        if (isNormalReligion){
            data = data.replace(" ","");
            data = data.replace(":","");
            agama.setValue(data);
            if (agama.getIndex() > -1) lastIndexValue++;
            isAgamaFound = true;
            return true;
        }
        return false;
    }

    private boolean getStatusPerkawinanData(String data){
        if (isStatusPerkawinanFound) return false;
        boolean isIdentifier = identifierChecker.isIdentifierStatusPerkawinan(data);
        if (isIdentifier && !data.contains(":") && statusPerkawinan.getIndex() < 0 && !data.toLowerCase().endsWith("kawin")){
            statusPerkawinan.setIndex(lastIndexIdentifier);
            lastIndexIdentifier++;
            identifiers.add("sp");
            return true;
        }
        if (data.toLowerCase().endsWith("kawin")){
            String dt = "";
            if (data.contains(":")){
                dt = data.split(":")[1];
            } else {
                if (data.toLowerCase().startsWith("belum") || data.toLowerCase().startsWith("kawin")){
                    dt = data;
                } else {
                    String[] spkw = data.split(" ");
                    if (spkw.length > 2) dt = spkw.length > 3 ? spkw[2]+spkw[3] : spkw[2];
                }
            }
            if (!dt.isEmpty()) {
                statusPerkawinan.setValue(dt);
                if (statusPerkawinan.getIndex() > -1) lastIndexValue++;
                isStatusPerkawinanFound = true;
            }
            return true;
        }
//        if (isIdentifier && data.contains(":")){
//            statusPerkawinan.setValue(data.split(":")[1]);
//            isStatusPerkawinanFound = true;
//            return true;
//        }
//        if (isIdentifier && data.split(" ").length > 2){
//            String[] stPks = data.split(" ");
//            String stPk = stPks.length == 3 ? stPks[2] : stPks[2]+stPks[3];
//            statusPerkawinan.setValue(stPk);
//            isStatusPerkawinanFound = true;
//            return true;
//        }
        return false;
    }

    private boolean getProvinsiData(String data){
        if (!provinsi.isEmpty()) return false;
        boolean isProvinsi = data.toLowerCase().contains("provinsi");
        if (isProvinsi){
            String[] provinsiDt = data.split(" ");
            String[] removeFirst = Arrays.copyOfRange(provinsiDt, 1, provinsiDt.length);
            provinsi = TextUtils.join(" ", removeFirst);
            return true;
        }
        return false;
    }

    private boolean getKabupatenKotaData(String data){
        if (!kabupatenKota.isEmpty()) return false;
        boolean isKabupatenKota = identifierChecker.isIdentifierKabKota(data);
        if (isKabupatenKota){
//            String[] kabKotaDt = data.split(" ");
//            String[] removeFirst = Arrays.copyOfRange(kabKotaDt, 1, kabKotaDt.length);
//            kabupatenKota = TextUtils.join(" ", removeFirst);
            kabupatenKota = data;
            return true;
        }
        if (data.toLowerCase().contains("jakarta")){
            kabupatenKota = data;
            return true;
        }
        return false;
    }

    private void getGolonganDarahData(String data){
        if (isGolonganDarahFound) return;
        boolean isIdentifier = identifierChecker.isIdentifierGolDarah(data);
        if (isIdentifier && !data.contains(":") && golonganDarah.getIndex() < 0 && data.endsWith("darah")){
            if (golonganDarah.getIndex() < 0){
                golonganDarah.setIndex(lastIndexIdentifier);
                lastIndexIdentifier++;
                identifiers.add("gd");
                return;
            }
        }
        String[] splitData = data.split(" ");
        if (isIdentifier && splitData.length > 2){
            if (data.contains(":")){
                golonganDarah.setValue(data.split(":")[1].replace(" ",""));
            } else {
                golonganDarah.setValue(splitData[2]);
            }
            isGolonganDarahFound = true;
        }
    }

    public String getProvinsi() {
        return provinsi;
    }

    public String getKabupatenKota() {
        return kabupatenKota;
    }

    public KTPData getNik() {
        return nik;
    }

    public KTPData getNama() {
        return nama;
    }

    public KTPData getTempatLahir() {
        return tempatLahir;
    }

    public KTPData getTanggalLahir() {
        return tanggalLahir;
    }

    public KTPData getJenisKelamin() {
        return jenisKelamin;
    }

    public KTPData getAlamat() {
        return alamat;
    }

    public KTPData getRtRw() {
        return rtRw;
    }

    public KTPData getKelDesa() {
        return kelDesa;
    }

    public KTPData getKecamatan() {
        return kecamatan;
    }

    public KTPData getAgama() {
        return agama;
    }

    public KTPData getStatusPerkawinan() {
        return statusPerkawinan;
    }

    public KTPData getPekerjaan() {
        return pekerjaan;
    }

    public KTPData getKewarganegaraan() {
        return kewarganegaraan;
    }

    public KTPData getBerlakuHingga() {
        return berlakuHingga;
    }

    public KTPData getGolonganDarah() {
        return golonganDarah;
    }
}
