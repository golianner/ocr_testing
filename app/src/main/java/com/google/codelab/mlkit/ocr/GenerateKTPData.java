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

    private int alamatLine = 0;

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
        data = data.toLowerCase();
        return data.equals("tempat/tgl lahir")
                || data.contains("tempa")
                || data.contains("tgl")
                || data.contains("lahir");
    }

    private boolean isIdentifierJenisKelamin(String data){
        data = data.toLowerCase();
        return data.equals("jenis kelamin")
                || data.startsWith("jenis") || data.endsWith("kelamin") || (data.startsWith("j") && data.split(" ")[0].length() == 5);
    }

    private boolean isIdentifierAlamat(String data){
        data = data.toLowerCase();
        return data.contains("alamat") || (data.contains("ala") && data.length() == 7);
    }

    private boolean isIdentifierRtRw(String data){
        data = data.toLowerCase();
        return data.equals("rt/rw")
                || data.startsWith("rt")
                || data.endsWith("rw");
    }

    private boolean isIdentifierKelDesa(String data){
        data = data.toLowerCase();
        return data.equals("kel/desa")
                || data.startsWith("kel")
                || data.endsWith("desa");
    }

    private boolean isIdentifierKecamatan(String data){
        data = data.toLowerCase();
        return data.equals("kecamatan") || data.startsWith("kec") || data.endsWith("matan");
    }

    private boolean isIdentifierAgama(String data){
        data = data.toLowerCase();
        return data.equals("agama")
                || data.startsWith("aga")
                || data.endsWith("ma");
    }

    private boolean isIdentifierStatusPerkawinan(String data){
        data = data.toLowerCase();
        return data.equals("status perkawinan") || data.startsWith("status")
                || data.endsWith("perkawinan");
    }

    private boolean isIdentifierKabKota(String data){
        data = data.toLowerCase();
        return data.contains("kabupaten") || data.contains("kota");
    }

    private boolean isIdentifierGolDarah(String data){
        data = data.toLowerCase();
        return data.equals("gol. darah") ||
                data.contains("gol") || data.contains("darah");
    }

    private boolean isNotIdentifierPekerjaan(String data){
        data = data.toLowerCase();
        return data.equals("pekerjaan") || data.startsWith("peker") || data.endsWith("kerjaan");
    }

    private boolean isNotIdentifierBerlakuHingga(String data){
        data = data.toLowerCase();
        return data.equals("berlaku hingga") || data.startsWith("berlaku") || data.endsWith("hingga");
    }
    
    private boolean isNotIdentifierKewarganegaraan(String data){
        data = data.toLowerCase();
        return data.equals("kewarganegaraan") || data.startsWith("kewarga") || data.endsWith("negaraan");
    }

    private boolean isNotIdentifier(String data){
        boolean result = data.equalsIgnoreCase("nik") || data.equals("Nama") || isIdentifierTempatTanggalLahir(data)
                || isIdentifierJenisKelamin(data) || isIdentifierAlamat(data) || isIdentifierRtRw(data)
                || isIdentifierKelDesa(data) || isIdentifierKecamatan(data) || isIdentifierAgama(data)
                || isIdentifierStatusPerkawinan(data) || isIdentifierGolDarah(data) || data.toLowerCase().contains("provinsi")
                || isIdentifierKabKota(data) || isNotIdentifierPekerjaan(data) || isNotIdentifierBerlakuHingga(data) || isNotIdentifierKewarganegaraan(data);
        return !result;
    }

    private boolean isValidJenisKelamin(String data){
        data = data.toLowerCase().replace(":","");
        return data.contains("laki-laki") || data.contains("perempuan")
                || data.startsWith("lak")
                || data.startsWith("per");
    }

    private boolean isValidRtRw(String data){
        return data.contains("/") && data.replace(":","").length() == 7 && isNumberExist(data);
    }

    private boolean isValidAgama(String data){
        return data.equalsIgnoreCase("islam") || data.equalsIgnoreCase("kristen")
                || data.equalsIgnoreCase("katolik") || data.equalsIgnoreCase("budha")
                || data.equalsIgnoreCase("hindu") || data.equalsIgnoreCase("konghuchu");
    }

    private boolean isValidGolDarah(String data){
        return data.equalsIgnoreCase("a") || data.equalsIgnoreCase("b") || data.equalsIgnoreCase("ab")
                || data.equalsIgnoreCase("o");
    }

    private boolean isPossibleAlamat(String data){
        data = data.toLowerCase();
        return data.contains("jl.") || data.contains("jalan ") || data.contains("no.") || data.contains("blok.") || data.contains("blok");
    }

    private boolean isPossibleDate(String data){
        return data.contains("-") && data.split("-").length == 3 && isNumberExist(data);
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
//            if (getAlamatLineTwo(data)) continue;
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
        System.out.println(Arrays.toString(identifiers.toArray()));
    }

    private boolean isPreviousAndNextFound(String identifier){
        int index = identifiers.indexOf(identifier);
        boolean prev = false, next = false;
        int lastIndex = identifiers.size() - 1;
        if (index < 0) return false;
        if (index > 0){
            String idtf = identifiers.get(index - 1);
            switch (idtf){
                case "nik":
                    prev = isNikFound;
                    break;
                case "nama":
                    prev = isNamaFound;
                    break;
                case "ttl":
                    prev = isTempatLahirFound;
                    break;
                case "alamat":
                    prev = isAlamatFound;
                    break;
                case "rtrw":
                    prev = isRtRwFound;
                    break;
                case "keldesa":
                    prev = isKelDesaFound;
                    break;
                case "kec":
                    prev = isKecamatanFound;
                    break;
                case "agama":
                    prev = isAgamaFound;
                    break;
                case "sp":
                    prev = isStatusPerkawinanFound;
                    break;
                case "gd":
                    prev = isGolonganDarahFound;
                    break;
            }
        }
        if (index < lastIndex){
            String idtf = identifiers.get(index + 1);
            switch (idtf){
                case "nik":
                    next = isNikFound;
                    break;
                case "nama":
                    next = isNamaFound;
                    break;
                case "ttl":
                    next = isTempatLahirFound;
                    break;
                case "alamat":
                    next = isAlamatFound;
                    break;
                case "rtrw":
                    next = isRtRwFound;
                    break;
                case "keldesa":
                    next = isKelDesaFound;
                    break;
                case "kec":
                    next = isKecamatanFound;
                    break;
                case "agama":
                    next = isAgamaFound;
                    break;
                case "sp":
                    next = isStatusPerkawinanFound;
                    break;
                case "gd":
                    next = isGolonganDarahFound;
                    break;
            }
        }
        return prev || next;
    }

    // Scpecial field : Jenis Kelamin, Golongan Darah, Agama
    private boolean exceptionForSpecialField(String data){
        data = data.toLowerCase();
        boolean result = isValidAgama(data) || isValidRtRw(data) || isValidJenisKelamin(data) || isValidGolDarah(data)
                || data.equals("kawin") || data.equals("belum kawin");
        return !result;
    }

    // Check if data contains any number
    private boolean isNumberExist(String data){
        Pattern pattern = Pattern.compile(".*\\d.*");
        Matcher matcher = pattern.matcher(data);
        return matcher.find();
    }

    private boolean getAlamatLineTwo(String data){
        if (alamatLine == 1){
            if (isNotIdentifier(data) && exceptionForSpecialField(data) && !isPossibleDate(data)){
                String newAlamat = alamat.getValue() + " " + data;
                alamat.setValue(newAlamat);
                alamatLine = 2;
                return true;
            }
        }
        return false;
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
        if (isNotIdentifier(data) && exceptionForSpecialField(data) && !isPossibleAlamat(data) && !isPossibleDate(data)){
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
        boolean isTempatTglLahir = isIdentifierTempatTanggalLahir(data);
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
        if (!data.contains(":") && data.contains("-") && isNumberExist(data) && data.split("-").length == 3){
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
            if (jenisKelamin.getIndex() > -1) lastIndexValue++;
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
            alamatLine = 1;
            return true;
        }
        if (isNotIdentifier(data) && exceptionForSpecialField(data)) {
            String dt = data.contains(":") ? data.replace(":","") : data;
            if (isPossibleAlamat(data)){
                alamat.setValue(dt);
                lastIndexValue++;
                isAlamatFound = true;
                alamatLine = 1;
                return true;
            } else if (lastIndexValue == alamat.getIndex()){
                alamat.setValue(dt);
                lastIndexValue++;
                isAlamatFound = true;
                alamatLine = 1;
                return true;
            } else if (isPreviousAndNextFound("alamat")){
                alamat.setValue(dt);
                lastIndexValue++;
                isAlamatFound = true;
                alamatLine = 1;
                return true;
            }
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
            if (rtRw.getIndex() > -1) lastIndexValue++;
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
        if (isNotIdentifier(data) && !isValidRtRw(data) && !isValidJenisKelamin(data) && !isValidAgama(data)
                && !isPossibleDate(data) && !isPossibleAlamat(data)){
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
        return false;
    }

    private boolean getKecamatanData(String data){
        if (isKecamatanFound) return false;
        boolean isIdentifier = isIdentifierKecamatan(data);
        if (isIdentifier && kecamatan.getIndex() < 0 && !data.contains(":")){
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
        if (isNotIdentifier(data) && !isValidRtRw(data) && !isValidJenisKelamin(data) && !isValidAgama(data)
                && !isPossibleDate(data) && !isPossibleAlamat(data)){
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
            if (agama.getIndex() > -1) lastIndexValue++;
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
            isStatusPerkawinanFound = true;
            return true;
        }
        if (isIdentifier && data.split(" ").length > 2){
            String[] stPks = data.split(" ");
            String stPk = stPks.length == 3 ? stPks[2] : stPks[2]+stPks[3];
            statusPerkawinan.setValue(stPk);
            isStatusPerkawinanFound = true;
            return true;
        }
        if (!isIdentifier && data.toLowerCase().endsWith("kawin")){
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
