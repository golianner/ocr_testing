package com.google.codelab.mlkit.ocr;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OCRValidator {

    public boolean isNumberExist(String data){
        Pattern pattern = Pattern.compile(".*\\d.*");
        Matcher matcher = pattern.matcher(data);
        return matcher.find();
    }
    public boolean isValidJenisKelamin(String data){
        data = data.toLowerCase().replace(":","");
        return data.contains("laki-laki") || data.contains("perempuan")
                || data.startsWith("lak") || data.contains("aki-lak") || data.endsWith("laki")
                || data.startsWith("per") || data.endsWith("rempuan");
    }

    public boolean isValidRtRw(String data){
        return data.contains("/") && data.replace(":","").length() == 7 && isNumberExist(data);
    }

    public boolean isValidAgama(String data){
        data = data.toLowerCase().replace(":","");
        data = data.replace(" ","");
        return data.equals("islam") || data.equals("kristen")
                || data.equals("katolik") || data.equals("budha")
                || data.equals("hindu") || data.equals("konghuchu");
    }

    public boolean isValidGolDarah(String data){
        data = data.toLowerCase().replace(":","");
        return data.equals("a") || data.equals("b") || data.equals("ab")
                || data.equals("o") || data.equals("0") || data.equals("4") || data.equals("48")
                || data.equals("4b") || data.equals("a8");
    }

    public boolean isPossibleAlamat(String data){
        data = data.toLowerCase();
        return data.contains("jl.") || data.contains("jalan ") || data.contains("no.") || data.contains("blok.") || data.contains("blok");
    }

    public boolean isPossibleDate(String data){
        return data.contains("-") && data.split("-").length == 3 && isNumberExist(data);
    }

    public boolean isPossiblePekerjaan(String data){
        data = data.toLowerCase();
        return data.contains("pelajar") || data.contains("mahasiswa") || data.contains("karyawan")
                || data.contains("bekerja");
    }

    public boolean isPossibleMasaBerlaku(String data){
        data = data.toLowerCase();
        return data.equals("seumur hidup") || data.startsWith("seumur") || data.endsWith("hidup");
    }

    public void validateData(String nik, String tanggalLahir, String rtRw, String golonganDarah,
                             String jenisKelamin, OCRValidator.ValueSetter setter){
        // NIK Validation
        String[] cari = {"l","I","z","Z","A","S","s","b","J","B","q","o","O",")"};
        String[] ganti = {"1","1","2","2","4","5","5","6","7","8","9","0","0","1"};
        for (int i = 0; i < cari.length; i++){
            nik = nik.replace(cari[i], ganti[i]);
        }
        setter.setNikValue(nik);
        // Tanggal lahir
        for (int i = 0; i < cari.length; i++){
            tanggalLahir = tanggalLahir.replace(cari[i], ganti[i]);
        }
        setter.setTanggalLahirValue(tanggalLahir);
        // RT/RW
        for (int i = 0; i < cari.length; i++){
            rtRw = rtRw.replace(cari[i], ganti[i]);
        }
        setter.setRtRwValue(rtRw);
        // Golongan Darah
        String[] cariAngka = {"4","0","8"};
        String[] gantiHuruf = {"A","O","B"};
        for (int i = 0; i < cariAngka.length; i++){
            golonganDarah = golonganDarah.replace(cariAngka[i], gantiHuruf[i]);
        }
        setter.setGolonganDarahValue(golonganDarah);
        // Jenis Kelamin
        if (jenisKelamin.toLowerCase().contains("lak")){
            jenisKelamin = "LAKI-LAKI";
        } else if (jenisKelamin.toLowerCase().contains("per") || jenisKelamin.toLowerCase().contains("puan") || jenisKelamin.contains("rem")){
            jenisKelamin = "PEREMPUAN";
        }
        setter.setJenisKelaminValue(jenisKelamin);
        setter.finishAll();
    }

    public interface ValueSetter {
        void setNikValue(String data);
        void setTanggalLahirValue(String data);
        void setRtRwValue(String data);
        void setGolonganDarahValue(String data);
        void setJenisKelaminValue(String data);

        void finishAll();
    }

}
