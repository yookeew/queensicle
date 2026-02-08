import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;
import java.util.List;

public class Main {

    public static void main(String[] args) throws Exception {
        WebDriverManager.chromedriver().setup();

        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter URL: ");
        String url = scanner.nextLine();

        System.out.print("Enter board size (e.g., 7): ");
        int gridSize = scanner.nextInt();

        WebDriver driver = new ChromeDriver();
        driver.get(url);
        Thread.sleep(2000);

        WebElement board = driver.findElement(By.className("board"));

        int boardX = board.getLocation().getX();
        int boardY = board.getLocation().getY();
        int boardWidth = board.getSize().getWidth();
        int boardHeight = board.getSize().getHeight();

        JavascriptExecutor js = (JavascriptExecutor) driver;
        double dpr = ((Number) js.executeScript("return window.devicePixelRatio")).doubleValue();

        File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        BufferedImage fullImg = ImageIO.read(screenshot);

        int sx = (int) (boardX * dpr);
        int sy = (int) (boardY * dpr);
        int sw = (int) (boardWidth * dpr);
        int sh = (int) (boardHeight * dpr);

        BufferedImage boardImg = fullImg.getSubimage(sx, sy, sw, sh);

        List<List<Integer>> colorBoard = parseBoard(boardImg, gridSize, 30);

        Solver solver = new Solver(new Puzzle(gridSize, colorBoard));
        List<List<Integer>> solution = solver.solve();

        if (solution == null) {
            System.out.println("No solution found");
            driver.quit();
            return;
        }

        Actions actions = new Actions(driver);

        for (List<Integer> pos : solution) {
            int row = pos.get(0);
            int col = pos.get(1);

            WebElement square = driver.findElement(
                    By.cssSelector(".square[data-row='" + row + "'][data-col='" + col + "']")
            );

            actions.moveToElement(square)
                    .doubleClick()
                    .perform();

            Thread.sleep(200);

        }

        System.out.println("Done!");
        Thread.sleep(5000);
        driver.quit();
    }

    static List<List<Integer>> parseBoard(BufferedImage img, int gridSize, int threshold) {
        int cellWidth = img.getWidth() / gridSize;
        int cellHeight = img.getHeight() / gridSize;

        List<List<Color>> colors = new ArrayList<>();

        for (int i = 0; i < gridSize; i++) {
            List<Color> row = new ArrayList<>();
            for (int j = 0; j < gridSize; j++) {

                double[] sx = {0.5, 0.3, 0.7, 0.3, 0.7};
                double[] sy = {0.5, 0.3, 0.3, 0.7, 0.7};

                int rSum = 0, gSum = 0, bSum = 0, count = 0;

                for (int s = 0; s < sx.length; s++) {
                    int x = (int) (j * cellWidth + cellWidth * sx[s]);
                    int y = (int) (i * cellHeight + cellHeight * sy[s]);

                    int rgb = img.getRGB(x, y);
                    int r = (rgb >> 16) & 0xFF;
                    int g = (rgb >> 8) & 0xFF;
                    int b = rgb & 0xFF;

                    if (!(r < 10 && g < 10 && b < 10)) {
                        rSum += r;
                        gSum += g;
                        bSum += b;
                        count++;
                    }
                }

                if (count > 0) {
                    row.add(new Color(rSum / count, gSum / count, bSum / count));
                } else {
                    row.add(Color.BLACK);
                }
            }
            colors.add(row);
        }

        List<Color> clusters = new ArrayList<>();

        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                Color c = colors.get(i).get(j);

                int id = -1;
                for (int k = 0; k < clusters.size(); k++) {
                    if (colorDistance(c, clusters.get(k)) < threshold) {
                        id = k;
                        break;
                    }
                }

                if (id == -1) {
                    clusters.add(c);
                    id = clusters.size() - 1;
                }

                colors.get(i).set(j, new Color(id, id, id));
            }
        }

        List<List<Integer>> board = new ArrayList<>();
        for (int i = 0; i < gridSize; i++) {
            List<Integer> row = new ArrayList<>();
            for (int j = 0; j < gridSize; j++) {
                row.add(colors.get(i).get(j).getRed());
            }
            board.add(row);
        }

        System.out.println("Detected " + clusters.size() + " unique colors");
        return board;
    }

    static double colorDistance(Color a, Color b) {
        return Math.sqrt(
                Math.pow(a.getRed() - b.getRed(), 2) +
                        Math.pow(a.getGreen() - b.getGreen(), 2) +
                        Math.pow(a.getBlue() - b.getBlue(), 2)
        );
    }
}
