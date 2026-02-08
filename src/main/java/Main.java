import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.time.Duration;
import java.util.*;
import java.util.List;

public class Main {
    static class RGB {
        int r, g, b, clusterId = -1;
        RGB(int r, int g, int b) { this.r = r; this.g = g; this.b = b; }
    }

    public static void main(String[] args) throws Exception {
        WebDriverManager.chromedriver().setup();

        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter URL: ");
        String url = scanner.nextLine();

        WebDriver driver = new ChromeDriver();
        driver.get(url);

        //Wait ONLY for the board to exist (not full DOM)
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        WebElement board = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.className("board"))
        );

        JavascriptExecutor js = (JavascriptExecutor) driver;
        String cols = (String) js.executeScript(
                "return getComputedStyle(arguments[0]).gridTemplateColumns",
                board
        );
        int gridSize = cols.trim().split("\\s+").length;

        System.out.println("Detected grid size: " + gridSize);

        int boardX = board.getLocation().getX();
        int boardY = board.getLocation().getY();
        int boardWidth = board.getSize().getWidth();
        int boardHeight = board.getSize().getHeight();

        double dpr = ((Number) js.executeScript("return window.devicePixelRatio")).doubleValue();

        File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        BufferedImage fullImg = ImageIO.read(screenshot);

        int sx = (int) (boardX * dpr);
        int sy = (int) (boardY * dpr);
        int sw = (int) (boardWidth * dpr);
        int sh = (int) (boardHeight * dpr);

        BufferedImage boardImg = fullImg.getSubimage(sx, sy, sw, sh);

        List<List<Integer>> colorBoard = parseBoard(boardImg, gridSize, 20);

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

            Thread.sleep(75);
        }

        System.out.println("Done!");
        Thread.sleep(5000);
        driver.quit();
    }

    static List<List<Integer>> parseBoard(BufferedImage img, int gridSize, int threshold) {
        int cellWidth = img.getWidth() / gridSize;
        int cellHeight = img.getHeight() / gridSize;

        List<List<RGB>> colors = new ArrayList<>();

        for (int i = 0; i < gridSize; i++) {
            List<RGB> row = new ArrayList<>();
            for (int j = 0; j < gridSize; j++) {

                int rSum = 0, gSum = 0, bSum = 0, count = 0;

                for (double sy = 0.3; sy <= 0.7; sy += 0.2) {
                    for (double sx = 0.3; sx <= 0.7; sx += 0.2) {

                        int x = (int) (j * cellWidth + cellWidth * sx);
                        int y = (int) (i * cellHeight + cellHeight * sy);

                        int rgb = img.getRGB(x, y);
                        int r = (rgb >> 16) & 0xFF;
                        int g = (rgb >> 8) & 0xFF;
                        int b = rgb & 0xFF;

                        if (!(r < 30 && g < 30 && b < 30)) {
                            rSum += r;
                            gSum += g;
                            bSum += b;
                            count++;
                        }
                    }
                }

                if (count == 0) {
                    row.add(new RGB(0, 0, 0));
                } else {
                    row.add(new RGB(
                            Math.round((float) rSum / count),
                            Math.round((float) gSum / count),
                            Math.round((float) bSum / count)
                    ));
                }
            }
            colors.add(row);
        }

        List<RGB> clusters = new ArrayList<>();

        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                RGB c = colors.get(i).get(j);

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

                c.clusterId = id;
            }
        }

        List<List<Integer>> board = new ArrayList<>();
        for (int i = 0; i < gridSize; i++) {
            List<Integer> row = new ArrayList<>();
            for (int j = 0; j < gridSize; j++) {
                row.add(colors.get(i).get(j).clusterId);
            }
            board.add(row);
        }

        System.out.println("Detected " + clusters.size() + " unique regions");
        printRegionGrid(board);

        return board;
    }


    static double colorDistance(RGB a, RGB b) {
        return Math.sqrt(
                Math.pow(a.r - b.r, 2) +
                        Math.pow(a.g - b.g, 2) +
                        Math.pow(a.b - b.b, 2)
        );
    }

    static void printRegionGrid(List<List<Integer>> board) {
        System.out.println("Detected regions grid:");
        for (List<Integer> row : board) {
            for (int cell : row) {
                System.out.print(cell + " ");
            }
            System.out.println();
        }
    }

}
