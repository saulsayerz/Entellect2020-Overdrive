# Tubes 1 S4RaP - Overdrive
> Halo! Selamat datang di repository Implementasi game engine overdrive S4RaP !!

Repository game engine ini dibuat untuk memenuhi **Tugas Besar Mata Kuliah IF2211 Strategi Algoritma** yang ke-1 pada Semester II Tahun Akademik 2021/2022. 

## Table of Contents
- [Tubes 1 S4RaP - Overdrive](#tubes-1-s4rap---overdrive)
  - [Table of Contents](#table-of-contents)
  - [Algoritma Greedy](#algoritma-greedy)
  - [Requirement and Installation](#requirement-and-installation)
  - [Compile Program](#compile-program)
  - [Author](#author)

## Algoritma Greedy
Dalam misi memenangkan permainan ini, algoritma greedy kami menggunakan obstacle sebagai fokus utama. Implementasinya kurang lebih adalah dengan menghindari obstacle yang ada di jalur mobil, kemudian meneliti jalur kanan dan jalur kiri mobil. Lalu, diliat mana yang *obstacle*-nya lebih sedikit dan *powerups*-nya lebih banyak, maka jalur itulah yang akan dipilih. Oleh sebab itu, dapat dikatakan bahwa algoritma greedy kami memiliki urutan fokus sebagai berikut :
1. Menghindari obstacle
2. Menggunakan powerups
3. Mengincar skor dengan memprioritaskan jalur yang memberikan lebih banyak benefit -> utamakan jalur tengah
4. Menambah kecepatan

## Requirement and Installation
Untuk dapat menjalankan permainan ini, maka pastikan perangkat sudah dilengkapi oleh aplikasi berikut :
1. [Bahasa Pemrograman Java (minimal Java 8)](https://www.oracle.com/java/technologies/downloads/#java8)
2. [IDE Intellij](https://www.jetbrains.com/idea/) / [VS Code](https://code.visualstudio.com/download) + [Maven](https://maven.apache.org/download.cgi) 
3. [NodeJS](https://nodejs.org/en/download/)

Permainan dapat dijalankan dengan cara :
1. Download file [starter-pack.zip](https://github.com/EntelectChallenge/2020-Overdrive/releases/tag/2020.3.4)
2. Unzip file pada mesin eksekusi
3. Clone repository ini ke dalam folder 
starter-pack
    ```bash
    git clone https://github.com/saulsayerz/Tubes1_S4RaP.git
    ```
4. Silahkan ganti lawan dari botnya secara manual pada `game-runner-config.json` (opsional).
5. Buka file yang bernama "run.bat" pada folder 
starter-pack
 atau ketik perintah 
./run
 pada command prompt dan permainan akan secara otomatis berjalan. 
6. Riwayat permainan akan muncul pada folder ```match-logs```. Oleh sebab itu, pastikan memori laptop anda cukup untuk mencatat riwayat permainan (kurang lebih 500mb).

## Compile Program
Untuk melakukan compile program pada VS Code, maka pastikan bahwa Maven sudah terunduh dan muncul pada sisi kiri layar VS Code. Kemudian, pilih "java-starter-bots". Setelah itu klik "Lifecycle" kemudian "Compile" dan "Install". Program akan secara otomatis melakukan build.

## Author
Project ini dibuat oleh kelompok 32 yang beranggotakan :
- Saul Sayers (13520094)
- Patrick Amadeus Irawan (13520109)
- Rania Dwi Fadhilah (13520142)