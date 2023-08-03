package com.google.codelab.mlkit.ocr;

import android.text.TextUtils;
import android.util.Log;

import com.google.mlkit.vision.text.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GenerateKTPData {

    private List<String> resultScan;
    private Text text;

    // Header
    private String provinsi = "";
    private String kabupatenKota = "";

    // Store data here
    private KTPData nik = new KTPData();
    private KTPData nama = new KTPData();
    private KTPData tempatLahir = new KTPData();
    private KTPData tanggalLahir = new KTPData();
    private KTPData jenisKelamin = new KTPData();
    private KTPData alamat = new KTPData();
    private KTPData rtRw = new KTPData();
    private KTPData kelDesa = new KTPData();
    private KTPData kecamatan = new KTPData();
    private KTPData agama = new KTPData();
    private KTPData statusPerkawinan = new KTPData();
    private KTPData pekerjaan = new KTPData();
    private KTPData kewarganegaraan = new KTPData();
    private KTPData berlakuHingga = new KTPData();
    private KTPData golonganDarah = new KTPData();

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
    private boolean isPekerjaanFound = false;
    private boolean isKewarganegaraanFound = false;
    private boolean isBerlakuHinggaFound = false;
    private boolean isGolonganDarahFound = false;

    List<String> identifiers = new ArrayList<>();
    private int lastIndexIdentifier = 0;
    private int lastIndexValue = 0;

    public GenerateKTPData(List<String> resultScan) {
        System.out.println(Arrays.toString(resultScan.toArray()));
        this.resultScan = resultScan;
        mappingData();
    }

    public GenerateKTPData(Text text) {
        this.text = text;
        mappingDataWithText();
    }

    private boolean isIdentifierTempatTanggalLahir(String data){
        return data.equalsIgnoreCase("tempat/tgl lahir")
                || data.contains("Tempa")
                || data.contains("Tgl")
                || data.contains("lahir")
                || data.length() <= 16;
    }

    private boolean isIdentifierJenisKelamin(String data){
        return data.equalsIgnoreCase("jenis kelamin")
                || data.contains("Jenis") || data.contains("Kelamin")
                || (data.split(" ")[0].toLowerCase().contains("j")
                && data.split(" ")[0].length() == 5);
    }

    private boolean isIdentifierAlamat(String data){
        return data.contains("Alamat") || (data.contains("Ala") && data.length() == 7);
    }

    private boolean isIdentifierRtRw(String data){
        return data.equalsIgnoreCase("rt/rw")
                || data.toLowerCase().startsWith("rt")
                || data.toLowerCase().endsWith("rw");
    }

    private boolean isIdentifierKelDesa(String data){
        return data.equalsIgnoreCase("kel/desa")
                || data.toLowerCase().startsWith("kel")
                || data.toLowerCase().endsWith("desa");
    }

    private boolean isIdentifierKecamatan(String data){
        return data.contains("Kecamatan") || data.contains("Kec") || data.contains("matan");
    }

    private boolean isIdentifierAgama(String data){
        return data.contains("Agama")
                || data.toLowerCase().startsWith("ag")
                || data.toLowerCase().endsWith("ma");
    }

    private boolean isIdentifierStatusPerkawinan(String data){
        return data.equals("Status Perkawinan") || data.startsWith("Status")
                || data.endsWith("Perkawinan");
    }

    private boolean isIdentifierKabKota(String data){
        return data.toLowerCase().contains("kabupaten") || data.toLowerCase().contains("kota");
    }

    private boolean isIdentifierGolDarah(String data){
        return data.equalsIgnoreCase("Gol. Darah") ||
                data.toLowerCase().contains("gol") || data.toLowerCase().contains("darah");
    }

    private boolean isNotIdentifier(String data){
        boolean result = data.equalsIgnoreCase("nik") || data.equals("Nama") || isIdentifierTempatTanggalLahir(data)
                || isIdentifierJenisKelamin(data) || isIdentifierAlamat(data) || isIdentifierRtRw(data)
                || isIdentifierKelDesa(data) || isIdentifierKecamatan(data) || isIdentifierAgama(data)
                || isIdentifierStatusPerkawinan(data) || isIdentifierGolDarah(data) || data.toLowerCase().contains("provinsi")
                || isIdentifierKabKota(data);
        return !result;
    }

    private boolean isValidJenisKelamin(String data){
        return data.toLowerCase().contains("laki-laki") || data.toLowerCase().contains("perempuan")
                || data.startsWith("lak")
                || data.startsWith("per");
    }

    private boolean isValidRtRw(String data){
        return data.contains("/") && data.length() == 7 && !data.contains(":") && isNumberExist(data);
    }

    private boolean isValidAgama(String data){
        return data.equalsIgnoreCase("islam") || data.equalsIgnoreCase("kristen")
                || data.equalsIgnoreCase("katolik") || data.equalsIgnoreCase("budha")
                || data.equalsIgnoreCase("hindu") || data.equalsIgnoreCase("konghuchu");
    }

    private void validateData(){
        // NIK Validation
        String data = nik.getValue();
        String[] cari = {"l","I","z","Z","A","S","s","b","J","B","q","o","O"};
        String[] ganti = {"1","1","2","2","4","5","5","6","7","8","9","0","0"};
        for (int i = 0; i < cari.length; i++){
            data = data.replace(cari[i], ganti[i]);
        }
        nik.setValue(data);
        // Tanggal lahir
        data = tanggalLahir.getValue();
        for (int i = 0; i < cari.length; i++){
            data = data.replace(cari[i], ganti[i]);
        }
        tanggalLahir.setValue(data);
        // RT/RW
        data = rtRw.getValue();
        for (int i = 0; i < cari.length; i++){
            data = data.replace(cari[i], ganti[i]);
        }
        rtRw.setValue(data);
        // Golongan Darah
        data = golonganDarah.getValue();
        String[] cariAngka = {"4","0","8"};
        String[] gantiHuruf = {"A","O","B"};
        for (int i = 0; i < cariAngka.length; i++){
            data = data.replace(cariAngka[i], gantiHuruf[i]);
        }
        golonganDarah.setValue(data);
        // Jenis Kelamin
        data = jenisKelamin.getValue();
        if (data.toLowerCase().contains("lak")){
            data = "LAKI-LAKI";
            jenisKelamin.setValue(data);
        } else if (data.toLowerCase().contains("per") || data.toLowerCase().contains("puan") || data.contains("rem")){
            data = "PEREMPUAN";
            jenisKelamin.setValue(data);
        }
        printResult();
    }

    private void mappingDataWithText(){
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
        validateData();
    }

    private void mappingData(){
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
        validateData();
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
    }

    // Scpecial field : Jenis Kelamin, Golongan Darah, Agama
    private boolean exceptionForSpecialField(String data){
        data = data.toLowerCase();
        boolean result =  data.equals("laki-laki") || data.equals("perempuan") || data.equals("a") || data.equals("b")
                || data.equals("ab") || data.equals("o") || data.equals("kawin") || data.equals("belum kawin")
                || data.equals("islam") || data.equals("kristen") || data.equals("katolik")
                || data.equals("budha") || data.equals("hindu") || data.equals("konghuchu")
                || data.startsWith("lak") || data.startsWith("per") || data.split("-").length == 3;
        return !result;
    }

    // Check if data contains any number
    private boolean isNumberExist(String data){
        Pattern pattern = Pattern.compile(".*\\d.*");
        Matcher matcher = pattern.matcher(data);
        return matcher.find();
    }

    private boolean getNIKData(String data){
        if (isNikFound) return false; // skip process if nik is already found
        boolean isNumberExist = isNumberExist(data);
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
        if (isNotIdentifier(data) && !isValidJenisKelamin(data) && !isValidRtRw(data) && !isValidAgama(data)
            && identifiers.indexOf("nama") == lastIndexValue && !data.contains(":")){
            nama.setValue(data);
            lastIndexValue++;
            isNamaFound = true;
            return true;
        }
        return false;
    }

    private boolean getTempatLahirData(String data){
        if (data.length() < 11) return false;
        boolean isTempatTglLahir = isIdentifierTempatTanggalLahir(data);
        if (isTempatLahirFound) return false;
        if (isTempatTglLahir && !data.contains(":") && tempatLahir.getIndex() < 0){
            tempatLahir.setIndex(lastIndexIdentifier);
            tanggalLahir.setIndex(lastIndexIdentifier);
            lastIndexIdentifier++;
            identifiers.add("ttl");
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
        if (lastIndexValue - 1 == tanggalLahir.getIndex() && data.split("-").length == 3){
            tanggalLahir.setValue(data);
            isTanggalLahirFound = true;
            return true;
        }
        return false;
    }

    private boolean getJenisKelaminData(String data){
        if (isJenisKelaminFound) return false;
        if (data.length() < 3) return false;
        boolean isIdentifier = isIdentifierJenisKelamin(data);
        if (data.toCharArray()[0] != ':' && isIdentifier && jenisKelamin.getIndex() < 0){
            jenisKelamin.setIndex(lastIndexIdentifier);
            lastIndexIdentifier++;
            identifiers.add("jk");
            return true;
        }
        if (data.toCharArray()[0] == ':' && lastIndexValue == jenisKelamin.getIndex()){
            jenisKelamin.setValue(data.replace(":", ""));
            lastIndexValue++;
            isJenisKelaminFound = true;
            return true;
        }
        boolean isValue = isValidJenisKelamin(data);
        if (isValue){
            jenisKelamin.setValue(data);
            lastIndexValue++;
            isJenisKelaminFound = true;
            return true;
        }
        return false;
    }

    private boolean getAlamatData(String data){
        if (isAlamatFound) return false;
        boolean isIdentifier = isIdentifierAlamat(data);
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
        if (!data.contains(":") && isNotIdentifier(data) && !isValidAgama(data) && !isValidRtRw(data) && !isValidJenisKelamin(data)
            && lastIndexValue == identifiers.indexOf("alamat")){
            alamat.setValue(data);
            lastIndexValue++;
            isAlamatFound = true;
            return true;
        }
        return false;
    }

    private boolean getRtRwData(String data){
        if (isRtRwFound) return false;
        boolean isIdentifier = isIdentifierRtRw(data);
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
        if (isValidRtRw(data)){
            rtRw.setValue(data);
            lastIndexValue++;
            isRtRwFound = true;
            return true;
        }
        return false;
    }

    private boolean getKelDesaData(String data){
        if (isKelDesaFound) return false;
        if (data.length() < 4) return false;
        boolean isIdentifier = isIdentifierKelDesa(data);
        if (isIdentifier && kelDesa.getIndex() < 0){
            kelDesa.setIndex(lastIndexIdentifier);
            lastIndexIdentifier++;
            identifiers.add("keldesa");
            return true;
        }
        if (data.toCharArray()[0] == ':' && lastIndexValue == kelDesa.getIndex()){
            kelDesa.setValue(data.replace(":", ""));
            lastIndexValue++;
            isKelDesaFound = true;
            return true;
        }
        return false;
    }

    private boolean getKecamatanData(String data){
        if (isKecamatanFound) return false;
        boolean isIdentifier = isIdentifierKecamatan(data);
        if (isIdentifier && kecamatan.getIndex() < 0){
            kecamatan.setIndex(lastIndexIdentifier);
            lastIndexIdentifier++;
            identifiers.add("kec");
            return true;
        }
        if (data.toCharArray()[0] == ':' && lastIndexValue == kecamatan.getIndex()){
            kecamatan.setValue(data.replace(":",""));
            lastIndexValue++;
            isKecamatanFound = true;
            return true;
        }
        return false;
    }

    private boolean getAgamaData(String data){
        if (isAgamaFound) return false;
        if (data.length() < 2) return false;
        boolean isIdentifier = isIdentifierAgama(data);
        if (isIdentifier && agama.getIndex() < 0){
            agama.setIndex(lastIndexIdentifier);
            lastIndexIdentifier++;
            identifiers.add("agama");
            return true;
        }
        if (data.toCharArray()[0] == ':' && lastIndexValue == agama.getIndex()){
            agama.setValue(data.replace(":",""));
            lastIndexValue++;
            isAgamaFound = true;
            return true;
        }
        // normal religion check
        boolean isNormalReligion = isValidAgama(data);
        if (isNormalReligion && !data.contains(":")){
            agama.setValue(data);
            lastIndexValue++;
            isAgamaFound = true;
            return true;
        }
        return false;
    }

    private boolean getStatusPerkawinanData(String data){
        if (isStatusPerkawinanFound) return false;
        boolean isIdentifier = isIdentifierStatusPerkawinan(data);
        if (isIdentifier && !data.contains(":") && statusPerkawinan.getIndex() < 0){
            statusPerkawinan.setIndex(lastIndexIdentifier);
            lastIndexIdentifier++;
            identifiers.add("sp");
            return true;
        }
        if (isIdentifier && data.contains(":")){
            statusPerkawinan.setValue(data.split(":")[1]);
            return true;
        }
        if (isIdentifier && data.split(" ").length > 2){
            String[] stPks = data.split(" ");
            String stPk = stPks.length == 3 ? stPks[2] : stPks[2]+stPks[3];
            statusPerkawinan.setValue(stPk);
            isStatusPerkawinanFound = true;
            return true;
        }
        if (!isIdentifier && data.toLowerCase().contains("kawin")){
            statusPerkawinan.setValue(data.replace(":",""));
            lastIndexValue++;
            isStatusPerkawinanFound = true;
            return true;
        }
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
        boolean isKabupatenKota = isIdentifierKabKota(data);
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

    private boolean getGolonganDarahData(String data){
        if (isGolonganDarahFound) return false;
        boolean isIdentifier = isIdentifierGolDarah(data);
        if (isIdentifier && !data.contains(":") && golonganDarah.getIndex() < 0){
            if (golonganDarah.getIndex() < 0){
                golonganDarah.setIndex(lastIndexIdentifier);
                lastIndexIdentifier++;
                identifiers.add("gd");
                return true;
            }
        }
        if (data.toCharArray()[0] == ':' && lastIndexValue == golonganDarah.getIndex()){
            golonganDarah.setValue(data.replace(":",""));
            lastIndexValue++;
            isGolonganDarahFound = true;
            return true;
        }
        if (isIdentifier && data.contains(":")){
            golonganDarah.setValue(data.split(":")[1]);
            isGolonganDarahFound = true;
            return true;
        }
        String[] splitData = data.split(" ");
        if (isIdentifier && splitData.length == 3){
            golonganDarah.setValue(splitData[2]);
            isGolonganDarahFound = true;
            return true;
        }
        return false;
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
