import java.io.*;
import java.net.*;
import java.text.DecimalFormat;
import com.sun.net.httpserver.*;

public class Main {
    public static void main(String[] args) throws Exception {
        // Tworzymy serwer HTTP na porcie 8080
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        
        // Dodajemy obsługę dla różnych ścieżek
        server.createContext("/", new MainHandler());
        server.createContext("/calculate", new CalculateHandler());
        
        server.setExecutor(null);
        server.start();
        
        System.out.println("Serwer uruchomiony na http://localhost:8080");
        System.out.println("Aby zatrzymać serwer, naciśnij Ctrl+C");
    }
}

// Obsługa strony głównej
class MainHandler implements HttpHandler {
    public void handle(HttpExchange exchange) throws IOException {
        String response = getMainPageHTML();
        
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
        exchange.sendResponseHeaders(200, response.getBytes("utf-8").length);
        
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes("utf-8"));
        os.close();
    }
    
    private String getMainPageHTML() {
        return "<!DOCTYPE html>" +
               "<html lang='pl'>" +
               "<head>" +
               "<meta charset='UTF-8'>" +
               "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
               "<title>Kalkulator Kredytowy</title>" +
               "<style>" +
               "body { font-family: Arial, sans-serif; max-width: 800px; margin: 50px auto; padding: 20px; background-color: #f5f5f5; }" +
               ".container { background: white; padding: 30px; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }" +
               "h1 { color: #333; text-align: center; margin-bottom: 30px; }" +
               ".form-group { margin-bottom: 20px; }" +
               "label { display: block; margin-bottom: 8px; font-weight: bold; color: #555; }" +
               "input[type='number'] { width: 100%; padding: 12px; border: 2px solid #ddd; border-radius: 5px; font-size: 16px; }" +
               "input[type='number']:focus { border-color: #4CAF50; outline: none; }" +
               "button { background-color: #4CAF50; color: white; padding: 12px 30px; border: none; border-radius: 5px; cursor: pointer; font-size: 16px; width: 100%; }" +
               "button:hover { background-color: #45a049; }" +
               ".info { background-color: #e7f3ff; padding: 15px; border-radius: 5px; margin-bottom: 20px; border-left: 4px solid #2196F3; }" +
               "</style>" +
               "</head>" +
               "<body>" +
               "<div class='container'>" +
               "<h1>🏠 Kalkulator Kredytowy</h1>" +
               "<div class='info'>" +
               "<strong>ℹ️ Informacja:</strong> Ten kalkulator pomoże Ci obliczyć miesięczną ratę kredytu na podstawie kwoty, oprocentowania i okresu spłaty." +
               "</div>" +
               "<form action='/calculate' method='POST'>" +
               "<div class='form-group'>" +
               "<label for='amount'>💰 Kwota kredytu (PLN):</label>" +
               "<input type='number' id='amount' name='amount' min='1000' max='10000000' step='100' required>" +
               "</div>" +
               "<div class='form-group'>" +
               "<label for='rate'>📈 Oprocentowanie roczne (%):</label>" +
               "<input type='number' id='rate' name='rate' min='0.1' max='50' step='0.1' required>" +
               "</div>" +
               "<div class='form-group'>" +
               "<label for='years'>📅 Okres spłaty (lata):</label>" +
               "<input type='number' id='years' name='years' min='1' max='50' step='1' required>" +
               "</div>" +
               "<button type='submit'>🧮 Oblicz ratę</button>" +
               "</form>" +
               "</div>" +
               "</body>" +
               "</html>";
    }
}

// Obsługa kalkulacji
class CalculateHandler implements HttpHandler {
    public void handle(HttpExchange exchange) throws IOException {
        if ("POST".equals(exchange.getRequestMethod())) {
            // Odczytujemy dane z formularza
            InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), "utf-8");
            BufferedReader br = new BufferedReader(isr);
            String formData = br.readLine();
            
            // Parsujemy dane
            String[] params = formData.split("&");
            double amount = 0, rate = 0;
            int years = 0;
            
            for (String param : params) {
                String[] keyValue = param.split("=");
                String key = URLDecoder.decode(keyValue[0], "UTF-8");
                String value = URLDecoder.decode(keyValue[1], "UTF-8");
                
                switch (key) {
                    case "amount":
                        amount = Double.parseDouble(value);
                        break;
                    case "rate":
                        rate = Double.parseDouble(value);
                        break;
                    case "years":
                        years = Integer.parseInt(value);
                        break;
                }
            }
            
            // Obliczamy ratę
            LoanCalculator calculator = new LoanCalculator();
            LoanResult result = calculator.calculate(amount, rate, years);
            
            // Generujemy odpowiedź
            String response = getResultPageHTML(amount, rate, years, result);
            
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
            exchange.sendResponseHeaders(200, response.getBytes("utf-8").length);
            
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes("utf-8"));
            os.close();
        }
    }
    
    private String getResultPageHTML(double amount, double rate, int years, LoanResult result) {
        DecimalFormat df = new DecimalFormat("#,##0.00");
        
        return "<!DOCTYPE html>" +
               "<html lang='pl'>" +
               "<head>" +
               "<meta charset='UTF-8'>" +
               "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
               "<title>Wyniki kalkulacji - Kalkulator Kredytowy</title>" +
               "<style>" +
               "body { font-family: Arial, sans-serif; max-width: 800px; margin: 50px auto; padding: 20px; background-color: #f5f5f5; }" +
               ".container { background: white; padding: 30px; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }" +
               "h1 { color: #333; text-align: center; margin-bottom: 30px; }" +
               ".result-box { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 25px; border-radius: 10px; margin: 20px 0; text-align: center; }" +
               ".result-value { font-size: 2.5em; font-weight: bold; margin: 15px 0; }" +
               ".details { background-color: #f8f9fa; padding: 20px; border-radius: 8px; margin: 20px 0; }" +
               ".detail-row { display: flex; justify-content: space-between; margin: 10px 0; padding: 8px 0; border-bottom: 1px solid #eee; }" +
               ".detail-label { font-weight: bold; color: #555; }" +
               ".detail-value { color: #333; }" +
               ".back-button { background-color: #6c757d; color: white; padding: 12px 30px; border: none; border-radius: 5px; cursor: pointer; font-size: 16px; text-decoration: none; display: inline-block; margin-top: 20px; }" +
               ".back-button:hover { background-color: #545b62; }" +
               ".warning { background-color: #fff3cd; color: #856404; padding: 15px; border-radius: 5px; margin: 20px 0; border-left: 4px solid #ffc107; }" +
               "</style>" +
               "</head>" +
               "<body>" +
               "<div class='container'>" +
               "<h1>📊 Wyniki Kalkulacji Kredytu</h1>" +
               "<div class='result-box'>" +
               "<h2>💳 Miesięczna rata</h2>" +
               "<div class='result-value'>" + df.format(result.monthlyPayment) + " PLN</div>" +
               "</div>" +
               "<div class='details'>" +
               "<h3>📋 Szczegóły kredytu</h3>" +
               "<div class='detail-row'>" +
               "<span class='detail-label'>💰 Kwota kredytu:</span>" +
               "<span class='detail-value'>" + df.format(amount) + " PLN</span>" +
               "</div>" +
               "<div class='detail-row'>" +
               "<span class='detail-label'>📈 Oprocentowanie roczne:</span>" +
               "<span class='detail-value'>" + rate + "%</span>" +
               "</div>" +
               "<div class='detail-row'>" +
               "<span class='detail-label'>📅 Okres spłaty:</span>" +
               "<span class='detail-value'>" + years + " lat (" + (years * 12) + " rat)</span>" +
               "</div>" +
               "<div class='detail-row'>" +
               "<span class='detail-label'>💵 Łączna kwota do spłaty:</span>" +
               "<span class='detail-value'>" + df.format(result.totalPayment) + " PLN</span>" +
               "</div>" +
               "<div class='detail-row'>" +
               "<span class='detail-label'>💸 Łączne odsetki:</span>" +
               "<span class='detail-value'>" + df.format(result.totalInterest) + " PLN</span>" +
               "</div>" +
               "</div>" +
               "<div class='warning'>" +
               "<strong>⚠️ Uwaga:</strong> To są orientacyjne obliczenia. Rzeczywiste warunki kredytu mogą się różnić w zależności od banku i Twojej sytuacji finansowej." +
               "</div>" +
               "<a href='/' class='back-button'>← Powrót do kalkulatora</a>" +
               "</div>" +
               "</body>" +
               "</html>";
    }
}

// Klasa do obliczeń kredytowych
class LoanCalculator {
    public LoanResult calculate(double principal, double annualRate, int years) {
        // Konwertujemy na miesięczne wartości
        double monthlyRate = (annualRate / 100) / 12;
        int numberOfPayments = years * 12;
        
        // Wzór na ratę annuitetową
        double monthlyPayment;
        if (monthlyRate == 0) {
            monthlyPayment = principal / numberOfPayments;
        } else {
            monthlyPayment = principal * (monthlyRate * Math.pow(1 + monthlyRate, numberOfPayments)) / 
                           (Math.pow(1 + monthlyRate, numberOfPayments) - 1);
        }
        
        double totalPayment = monthlyPayment * numberOfPayments;
        double totalInterest = totalPayment - principal;
        
        return new LoanResult(monthlyPayment, totalPayment, totalInterest);
    }
}

// Klasa przechowująca wyniki obliczeń
class LoanResult {
    public final double monthlyPayment;
    public final double totalPayment;
    public final double totalInterest;
    
    public LoanResult(double monthlyPayment, double totalPayment, double totalInterest) {
        this.monthlyPayment = monthlyPayment;
        this.totalPayment = totalPayment;
        this.totalInterest = totalInterest;
    }
}