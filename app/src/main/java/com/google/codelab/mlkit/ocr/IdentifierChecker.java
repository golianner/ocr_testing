package com.google.codelab.mlkit.ocr;

public class IdentifierChecker {
    public boolean isIdentifierTempatTanggalLahir(String data){
        data = data.toLowerCase();
        return data.equals("tempat/tgl lahir")
                || data.contains("tempa")
                || data.contains("tgl")
                || data.contains("lahir");
    }

    public boolean isIdentifierJenisKelamin(String data){
        data = data.toLowerCase();
        return data.equals("jenis kelamin")
                || data.startsWith("jenis") || data.endsWith("kelamin") || (data.startsWith("j") && data.split(" ")[0].length() == 5);
    }

    public boolean isIdentifierAlamat(String data){
        data = data.toLowerCase();
        return data.contains("alamat") || (data.contains("ala") && data.length() == 7);
    }

    public boolean isIdentifierRtRw(String data){
        data = data.toLowerCase();
        return data.equals("rt/rw")
                || data.startsWith("rt")
                || data.endsWith("rw");
    }

    public boolean isIdentifierKelDesa(String data){
        data = data.toLowerCase();
        return data.equals("kel/desa")
                || data.startsWith("kel")
                || data.endsWith("desa");
    }

    public boolean isIdentifierKecamatan(String data){
        data = data.toLowerCase();
        return data.equals("kecamatan") || data.startsWith("kec") || data.endsWith("matan");
    }

    public boolean isIdentifierAgama(String data){
        data = data.toLowerCase();
        return data.equals("agama")
                || data.startsWith("aga")
                || data.endsWith("ma");
    }

    public boolean isIdentifierStatusPerkawinan(String data){
        data = data.toLowerCase();
        return data.equals("status perkawinan") || data.startsWith("status")
                || data.endsWith("perkawinan");
    }

    public boolean isIdentifierKabKota(String data){
        data = data.toLowerCase();
        return data.contains("kabupaten") || data.contains("kota");
    }

    public boolean isIdentifierGolDarah(String data){
        data = data.toLowerCase();
        return data.equals("gol. darah") ||
                data.contains("gol") || data.contains("darah");
    }

    public boolean isNotIdentifierPekerjaan(String data){
        data = data.toLowerCase();
        return data.equals("pekerjaan") || data.startsWith("peker") || data.endsWith("kerjaan");
    }

    public boolean isNotIdentifierBerlakuHingga(String data){
        data = data.toLowerCase();
        return data.equals("berlaku hingga") || data.startsWith("berlaku") || data.endsWith("hingga");
    }

    public boolean isNotIdentifierKewarganegaraan(String data){
        data = data.toLowerCase();
        return data.equals("kewarganegaraan") || data.startsWith("kewarga") || data.endsWith("negaraan");
    }
}
