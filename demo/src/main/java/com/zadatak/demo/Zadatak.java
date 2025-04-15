package com.zadatak.demo;

import java.util.Scanner;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class Zadatak {
	public static void main(String[] args){
		// Otvoriti dva terminala: jedan kao server i drugi kao klijent
		System.out.print("Unos recenzije ili produkta? (y za produkt, n za recenziju, bilo što drugo za dohvat produkta): ");
		Scanner sc = new Scanner(System.in);
		String unos = sc.next();

		// Dio za produkt
		if(unos.equals("y")){
			String code = "";
			boolean flag = true;
			while(flag){
				System.out.print("\nNapišite kod produkta (15 znakova): ");
				code = sc.nextLine();
				if(code.length() == 15)
					flag = false;
				else
					System.out.println("Kod nema 15 znakova, molimo ponoviti");
			}
			System.out.print("\nNapišite svoje ime: ");
			String name = sc.nextLine();
			System.out.print("\nNapišite opis produkta: ");
			String description = sc.nextLine();
			System.out.print("\nNapišite cijenu (EUR): ");
			float priceEUR = sc.nextFloat();

			String request = "http://localhost:8080/addProduct?name=" + name + "&code=" + code + "&description=" + description + "&price=" + priceEUR;
			RestTemplate restTemplate = new RestTemplate();
			ResponseEntity responseAPI = restTemplate.getForEntity(request, String.class);
			System.out.println(responseAPI.getBody());
		}
		// Dio za recenziju
		else if (unos.equals("n")) {
			Scanner reader = new Scanner(System.in);

			boolean flag = true;
			int rating = 0;

			System.out.print("Napisite svoje ime: ");
			String name = reader.nextLine();
			System.out.print("\nKoji ID produkta?: ");
			int productId = reader.nextInt();
			System.out.print("\nVasa recenzija: ");
			String review = reader.nextLine();
			while(flag){
				System.out.print("\nOcjena produkta (1-5): ");
				rating = reader.nextInt();

				if(rating >= 1 || rating <= 5)
					flag = false;
				else
					System.out.println("Recenzija nije unutar raspona, molimo ponoviti");
			}

			String request = "http://localhost:8080/addReview?name=" + name + "&productId=" + productId + "&review=" + review + "&rating=" + rating;
			//System.out.println(request);
			RestTemplate restTemplate = new RestTemplate();
			ResponseEntity responseAPI = restTemplate.getForEntity(request, String.class);
			System.out.println(responseAPI.getBody());
		}
		// Dio za dohvat podataka
		else {
			String request = "http://localhost:8080/getProducts";
			RestTemplate restTemplate = new RestTemplate();
			ResponseEntity responseAPI = restTemplate.getForEntity(request, String.class);
			System.out.println(responseAPI.getBody());
		}
	}
}
