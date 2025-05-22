import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;

public class TataTaiko extends JPanel implements KeyListener {
    int screen = 1;
    Image TitleImage;
    Image CursorMenu;
    Image SongSelectBackground;
    Image InGameBackground;
    Image TaikoDrum;
    Image DonDrum;
    Image KaDrum;
    Image Lane;
    Image BackgroundFinished;
    Image JudgeMeter;

    // small notes
    Image noteDon;
    Image noteKa;
    // big notes
    Image noteDonBig;
    Image noteKaBig;

    //scoring images
    Image imgGood;
    Image imgOK;
    Image imgBad;

    // judgment display variables
    Image currentJudgmentImage;
    long judgmentDisplayTime = 0;
    final int judgmentDisplayDuration = 800;
    int gameScore = 0;
    int comboScore = 0;
    // Hit counters (for display)
    int countGood = 0;
    int countOK = 0;
    int countBad = 0;
    // custom mp3 player object handles sound for the game. except for don/ka sounds which creates temp objects.
    MP3Player mp3Player;

    int cursorPosition = 0;
    String songPath = "Songs/";
    File song = new File(songPath);
    String songName = "";
    ArrayList <String> binFilesList = new ArrayList <String> ();
    boolean binFilesLoaded = false;
    boolean drawDonRight = false;
    boolean drawDonLeft = false;
    boolean drawKaRight = false;
    boolean drawKaLeft = false;
    final int END_DELAY = 3500; // 3.5 seconds delay after the last note

    // note timing and movement variables
    ArrayList <Note> notes = new ArrayList <> ();
    long songStartTime;
    Timer gameTimer;
    final int travelTime = 2000;
    final int laneLeftX = 10;
    final int laneRightX = 40 + 1600;

    // timing windows (ms)
    final int GOOD_WINDOW = 25;
    final int OK_WINDOW = 70;
    final int MAX_WINDOW = 108;

    // judging circle radius
    final int judgeRadius = 50;

    // hit target offset
    final int hitOffsetX = 150;

    // To track last note time.
    long lastNoteTime = 0;
    MP3Player themeSong = new MP3Player("Assets/Sounds/theme.mp3");
    boolean themeSongPlayed = false;


    // prevent keyholding to spam it
    private boolean sHeld = false;
    private boolean kHeld = false;
    private boolean aHeld = false;
    private boolean lHeld = false;

    public TataTaiko() {
        // constructor that loads images, and sets up frames
        addKeyListener(this);
        setFocusable(true);
        this.requestFocusInWindow();

        try {
            TitleImage = ImageIO.read(new File("Assets/1_Title/Background.png"));
            CursorMenu = ImageIO.read(new File("Assets/1_Title/Cursor_Right.png"));
            SongSelectBackground = ImageIO.read(new File("Assets/2_SongSelect/Background.png"));
            InGameBackground = ImageIO.read(new File("Assets/3_InGame/Background.png"));
            TaikoDrum = ImageIO.read(new File("Assets/3_InGame/Taiko/Base.png"));
            DonDrum = ImageIO.read(new File("Assets/3_InGame/Taiko/Don.png"));
            KaDrum = ImageIO.read(new File("Assets/3_InGame/Taiko/Ka.png"));
            Lane = ImageIO.read(new File("Assets/3_InGame/Lane.png"));
            BackgroundFinished = ImageIO.read(new File("Assets/4_GameDone/Background.png"));
            JudgeMeter = ImageIO.read(new File("Assets/4_GameDone/JudgeMeter.png"));
            noteDon = ImageIO.read(new File("Assets/3_InGame/Don.png"));
            noteKa = ImageIO.read(new File("Assets/3_InGame/Ka.png"));
            noteDonBig = ImageIO.read(new File("Assets/3_InGame/DonBig.png"));
            noteKaBig = ImageIO.read(new File("Assets/3_InGame/KaBig.png"));
            imgGood = ImageIO.read(new File("Assets/3_InGame/Good.png"));
            imgOK = ImageIO.read(new File("Assets/3_InGame/OK.png"));
            imgBad = ImageIO.read(new File("Assets/3_InGame/Bad.png"));
        } catch (IOException e) {
            System.out.println("Error loading images, some assets could not be loaded");
        }
        System.out.println("noteDonBig: " + noteDonBig.getWidth(null) + "x" + noteDonBig.getHeight(null));
        System.out.println("noteKaBig: " + noteKaBig.getWidth(null) + "x" + noteKaBig.getHeight(null));
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        switch(screen){
            // title page
            case 1:
                if (key == KeyEvent.VK_ENTER) {
                    playDonSound();
                }
                handleMenuNavigation(e);
                break;
            // song select
            case 2:
                if (key == KeyEvent.VK_ENTER) {
                    playDonSound();
                }
                handleSongSelection(e);
                break;
        // game area
            case 3:
                // Back to title
                if (key == KeyEvent.VK_ESCAPE) {
                    screen = 1;
                    mp3Player.stop();
                    if (gameTimer != null) gameTimer.stop();
                    countGood = 0;
                    countOK = 0;
                    comboScore = 0;
                    gameScore = 0;
                    break;
                }

                // Don left (S)
                if (key == KeyEvent.VK_S) {
                    if (!sHeld) {
                        sHeld = true;
                        playDonSound();
                        checkHit(e);
                        HandleGameBoard(e);
                    }
                    break;
                }
                // Don right (K)
                if (key == KeyEvent.VK_K) {
                    if (!kHeld) {
                        kHeld = true;
                        playDonSound();
                        checkHit(e);
                        HandleGameBoard(e);
                    }
                    break;
                }
                // Ka left (A)
                if (key == KeyEvent.VK_A) {
                    if (!aHeld) {
                        aHeld = true;
                        playKaSound();
                        checkHit(e);
                        HandleGameBoard(e);
                    }
                    break;
                }
                // Ka right (L)
                if (key == KeyEvent.VK_L) {
                    if (!lHeld) {
                        lHeld = true;
                        playKaSound();
                        checkHit(e);
                        HandleGameBoard(e);
                    }
                    break;
                }
                break;
        // end screen
            case 4:
                if (key == KeyEvent.VK_ESCAPE) {
                    System.exit(0);
                } else if (key == KeyEvent.VK_ENTER) {
                    screen = 1;
                    cursorPosition = 0;
                }
                break;
        }

        repaint();
    }

    private void playDonSound() {
        MP3Player temp = new MP3Player("Assets/Sounds/Don.mp3");
        temp.play();
    }

    private void playKaSound() {
        MP3Player temp = new MP3Player("Assets/Sounds/Ka.mp3");
        temp.play();
    }

    // moves cursor image up or down depending on arrow keys, calls binloader.
    private void handleMenuNavigation(KeyEvent e) {
        // clamp to keep cursor position in between 0 and 2 to prevent overflows of the array.
        if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            cursorPosition = Math.min(cursorPosition + 1, 2);
        } else if (e.getKeyCode() == KeyEvent.VK_UP) {
            cursorPosition = Math.max(cursorPosition - 1, 0);
        }
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            // title screen cursor checks, cursorpos 0 is play game, cursor pos 1 is BIN-TJA converter, cursorpos 2 is exit
            if (cursorPosition == 2 && screen == 1)
                System.exit(0);
            if (cursorPosition == 0 && screen == 1) {
                screen = 2;
                cursorPosition = 0;
                if (!binFilesLoaded) {
                    loadBinFiles();
                    binFilesLoaded = true;
                }
            }
            if(cursorPosition == 1 && screen == 1){
                TJAtoBin.main(null);
            }
        }
    }

    // handles the song selection menu, which will also move us into the real game
    private void handleSongSelection(KeyEvent e) {
        if (!binFilesList.isEmpty()) {
            if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                cursorPosition = Math.min(cursorPosition + 1, binFilesList.size());
            } else if (e.getKeyCode() == KeyEvent.VK_UP) {
                cursorPosition = Math.max(cursorPosition - 1, 0);
            } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                if (cursorPosition == binFilesList.size()) {
                    screen = 1;
                    cursorPosition = 0;
                } else {
                    System.out.println("Selected song: " + binFilesList.get(cursorPosition));
                    loadSong(binFilesList.get(cursorPosition));
                }
            }
        }
    }

    // overriden paint method to draw the differnet screens. called every repaint cycle
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (screen == 1)
            drawTitleScreen(g);
        else if (screen == 2)
            drawSongSelect(g);
        else if (screen == 3)
            drawGameboard(g);
        else if (screen == 4)
            drawEndScreen(g);
    }

    // draws title screen
    public void drawTitleScreen(Graphics g) {
        if (!themeSongPlayed) {
            themeSong.play();
            themeSongPlayed = true;
        }
        g.drawImage(TitleImage, 0, 0, getWidth(), getHeight(), this);
        // y-positions for the title screen
        int[] positions = {
                870,
                920,
                970
        };
        drawCursorMenu(g, positions);
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 40));
        g.drawString("Song Select", 900, 900);
        g.drawString("TJA to Bin", 900, 950);
        g.drawString("Exit",  900, 1000);
    }

    // draws the cursor
    public void drawCursorMenu(Graphics g, int[] yPositions) {
        // clamp to keep the cursorposition in between the actual areas, fixes arrayoutofbounds issues
        if (cursorPosition < 0) {
            cursorPosition = 0;
        } else if (cursorPosition >= yPositions.length) {
            cursorPosition = yPositions.length - 1;
        }
        // song select
        if (screen == 2) {
            int cursorX = getWidth() / 2 - 250; // offsets 250 pixels away from the middle
            g.drawImage(CursorMenu, cursorX, yPositions[cursorPosition] - 25, this);
            // title screen
        } else if (screen == 1) {
            g.drawImage(CursorMenu, 800, yPositions[cursorPosition], this);
        }
    }
    // draws song select while stripping out filenames, uses fontmetrics to get pixel sizes of strings
    // for making string sizing easier
    public void drawSongSelect(Graphics g) {
        g.drawImage(SongSelectBackground, 0, 0, getWidth(), getHeight(), this);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Meiryo", Font.BOLD, 40));
        FontMetrics fm = g.getFontMetrics();
        String title = "曲の選択 (Song Selection)";
        // gets ideal width of the title
        int titleWidth = fm.stringWidth(title);
        int titleX = (getWidth() - titleWidth) / 2;
        g.drawString(title, titleX, 100);
        int yOffset = 200;
        int lineHeight = 50;
        int[] yPositions = new int[binFilesList.size() + 1];
        for (int i = 0; i < binFilesList.size(); i++) {
            // name without the .bin, cleaner for the user
            String sName = binFilesList.get(i).substring(0, binFilesList.get(i).length() - 4);
            int textWidth = fm.stringWidth(sName);
            int x = (getWidth() - textWidth) / 2;
            g.drawString(sName, x, yOffset);
            yPositions[i] = yOffset;
            yOffset += lineHeight;
        }
        String exitText = "Exit";
        int exitWidth = fm.stringWidth(exitText);
        int exitX = (getWidth() - exitWidth) / 2;
        g.drawString(exitText, exitX, yOffset);
        yPositions[binFilesList.size()] = yOffset;
        drawCursorMenu(g, yPositions);
    }

    // loads all bin files found in subdirectories in the "songs" folder, also prints the amount found for debugging.
    private void loadBinFiles() {
        binFilesList.clear();
        if (song.exists() && song.isDirectory()) {
            // goes thru and adds all directories to a seperate array, kinda like a concicsed loop.
            File[] subFolders = song.listFiles(File::isDirectory);
            if (subFolders != null) {
                for (File subFolder: subFolders) {
                    // this lambda takes each file’s name, lowercases it, and checks if it ends with .bin, the format of the charts
                    // if it does, it keeps that file in the returned array
                    File[] binFiles = subFolder.listFiles((dir, name) -> name.toLowerCase().endsWith(".bin"));
                    if (binFiles != null) {
                        for (File binFile: binFiles) {
                            binFilesList.add(binFile.getName());
                        }
                    }
                }
            }
        }
        System.out.println("Loaded " + binFilesList.size() + " .bin files.");
    }

    // loads the song and starts game timer, which then starts updating notes.
    public void loadSong(String song) {
        this.songName = new File(song).getName();
        System.out.println("Loading song: " + songName);
        screen = 3;
        repaint();
        if (mp3Player != null) {
            mp3Player.stop();
        }
        String songFolder = songName.replace(".bin", "");
        String mp3FilePath = "Songs/" + songFolder + "/" + songFolder + ".mp3";
        mp3Player = new MP3Player(mp3FilePath);
        mp3Player.play();
        loadNotes("Songs/" + songFolder + "/" + songName);
        songStartTime = System.currentTimeMillis();
        // game timer, repaints every 7 milliseconds, found as a good way to match 144fps targets as without this,
        // the repaint method would only be called when java deems it to be neccesary, which will result in low framerates,
        // which is undeseriable in a rythmn game, also updates note positions.
        gameTimer = new Timer(7, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                updateNotes();
                repaint();
            }
        });
        gameTimer.start();
    }

    // loads notes from bin file into a list
    public void loadNotes(String filePath) {
        themeSong.stop();
        themeSongPlayed = false;
        notes.clear();
        lastNoteTime = 0;

        // opens new scanner and goes thru the lines until it finds the note timing line.
        try (Scanner scanner = new Scanner(new File(filePath))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.startsWith("Note Timing:")) {
                    break;
                }
            }

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.isEmpty()) continue;

                String[] parts = line.split(" ");
                if (parts.length < 2) continue;

                long hitTime = TimeToMs(parts[0]);
                int type = Integer.parseInt(parts[1]);

                Note note;
                // switch case to determine type and instantiate accordingly
                switch (type) {
                    case 0: // small Don
                        note = new RedNote(hitTime, false, laneRightX, noteDon);
                        break;
                    case 1: // small Ka
                        note = new BlueNote(hitTime, false, laneRightX, noteKa);
                        break;
                    case 2: // big Don
                        note = new RedNote(hitTime, true, laneRightX, noteDonBig);
                        break;
                    case 3: // big Ka
                        note = new BlueNote(hitTime, true, laneRightX, noteKaBig);
                        break;
                    default:
                        // game crash if unknown note type is found.
                        System.out.println("Unknown note type: " + type);
                        note = null; // if this gets added tho, game gets a nullptr exception
                }

                notes.add(note); // add the note

                if (hitTime > lastNoteTime) {
                    lastNoteTime = hitTime;
                }
            }
        } catch (FileNotFoundException e) {
            System.err.println("Note file not found: " + filePath);
        }
    }
// takes time in MM:SS:MS and returns back time an int which is total time in Ms
    public long TimeToMs(String timeStr) {
        String[] parts = timeStr.split(":");
        int minutes = Integer.parseInt(parts[0]);
        int seconds = Integer.parseInt(parts[1]);
        int milliseconds = Integer.parseInt(parts[2]);
        return minutes * 60000 + seconds * 1000 + milliseconds;
    }

    public void updateNotes() {
        // get elapsed time since song start
        long currentTime = System.currentTimeMillis() - songStartTime;
        // create iterator for notes arraylist, kind of like a different type of arrayList that is easier to iterate over
        Iterator <Note> iter = notes.iterator();
        // for each note in notes
        while (iter.hasNext()) {
            // go to next note
            Note note = iter.next();
            // compute target x position for this note
            int targetX = laneLeftX + hitOffsetX;

            // if note has passed hit window
            if (currentTime >= note.hitTime + MAX_WINDOW) {
                String kind = note.getNoteKind();
                // only big notes get scored even after being hit
                if (kind.equals("bigDon") || kind.equals("bigKa")) {
                    // if one side of this big note was hit
                    if (note.leftHit || note.rightHit) {
                        long error;
                        // determine which side to use
                        if (note.leftHit && !note.rightHit) {
                            error = note.leftError;
                        } else if (!note.leftHit && note.rightHit) {
                            error = note.rightError;
                        } else {
                            // both sides were hit pick better timing (picks min between 2 values)
                            error = Math.min(note.leftError, note.rightError);
                        }

                        // gives score based on where it was hit
                        if (error <= GOOD_WINDOW) {
                            gameScore += 960;
                            comboScore++;
                        } else {
                            gameScore += 420;
                            comboScore++;
                        }
                    }
                }
                // mark missed hit
                currentJudgmentImage = imgBad;
                judgmentDisplayTime = System.currentTimeMillis();
                countBad++;
                // reset combo
                comboScore = 0;
                // remove note from list
                iter.remove();
                continue;
            }


            // calculates how far the note has gone left to right based on the current time
            // "fraction" is the division of time passed between the start and hit time
            // changeX is the horizontal distance the note has to travel
            double fraction = (double)(currentTime - (note.hitTime - travelTime)) / travelTime;
            // update x area based on its travel fraction
            int changeX = targetX - laneRightX;
            note.x = laneRightX + (int)(fraction * changeX);
        }

        // if no notes remain and end delay passed
        if (notes.isEmpty() && currentTime > lastNoteTime + MAX_WINDOW + END_DELAY) {
            // move to results screen
            screen = 4;
            // stop game timer if running
            if (gameTimer != null) {
                gameTimer.stop();
            }
        }
    }

    //handles user input and judges against the note timing for small and big notes
    private void checkHit(KeyEvent e) {
        // return current time related to millis (number of miliseconds since Jan 1, 1970, UTC, and when we started the song
        // this is a standard for some reason lol
        long now = System.currentTimeMillis() - songStartTime;
        int key = e.getKeyCode();

        boolean hitDon = (key == KeyEvent.VK_S || key == KeyEvent.VK_K);
        boolean hitKa = (key == KeyEvent.VK_A || key == KeyEvent.VK_L);
        // skips over all checks if nothing was hit, saves resources
        if (!hitDon && !hitKa) return;
        // cast to note type
        for (Note note: new ArrayList <> (notes)) {
            // did the user hit the wrong note type
            if (hitDon && !note.isDon()) continue;
            if (hitKa && !note.isKa()) continue;

            // how delayed/early the hit was
            long error = Math.abs(note.hitTime - now);
            if (error > MAX_WINDOW) continue;

            // small vs Big
            if (!note.isBig()) {
                // small Don or small Ka
                notes.remove(note);
                if (error <= GOOD_WINDOW) {
                    currentJudgmentImage = imgGood;
                    gameScore += 960;
                    countGood++;
                    comboScore++;
                } else if (error <= OK_WINDOW) {
                    currentJudgmentImage = imgOK;
                    gameScore += 420;
                    countOK++;
                    comboScore++;
                } else {
                    currentJudgmentImage = imgBad;
                    countBad++;
                    comboScore = 0;
                }
                judgmentDisplayTime = System.currentTimeMillis();
            } else {
                // big Don or big Ka: need two keys
                // record left/right hit
                if (hitDon) {
                    if (key == KeyEvent.VK_S && !note.leftHit) {
                        note.leftHit = true;
                        note.leftError = error;
                    } else if (key == KeyEvent.VK_K && !note.rightHit) {
                        note.rightHit = true;
                        note.rightError = error;
                    } else {
                        continue;
                    }
                } else {
                    // hitKa
                    if (key == KeyEvent.VK_A && !note.leftHit) {
                        note.leftHit = true;
                        note.leftError = error;
                    } else if (key == KeyEvent.VK_L && !note.rightHit) {
                        note.rightHit = true;
                        note.rightError = error;
                    } else {
                        continue;
                    }
                }

                // if both halves are down, judge and remove
                if (note.leftHit && note.rightHit) {
                    if (note.leftError <= GOOD_WINDOW && note.rightError <= GOOD_WINDOW) {
                        currentJudgmentImage = imgGood;
                        gameScore += 1820;
                        countGood++;
                        comboScore++;
                    } else {
                        currentJudgmentImage = imgOK;
                        gameScore += 420;
                        countOK++;
                        comboScore++;
                    }
                    judgmentDisplayTime = System.currentTimeMillis();
                    notes.remove(note);
                }
            }

            // only one note per press
            return;
        }
    }

    public void HandleGameBoard(KeyEvent e) {
        // read which key was pressed
        int keyCode = e.getKeyCode();
        // how long (in milliseconds) to display the hit animation
        int duration = 100;

        // THE CODE HERE IS REALLY BAD AND WILL LEAD TO MEMORY LEAKS AND INEFFICIENT OBJECT MANAGEMENT
        if (keyCode == KeyEvent.VK_S) {
            // left don
            drawDonLeft = true;
            repaint();
            // starts a swing timer for 100ms to display the left side don, then turns it off and repaints
            new Timer(duration, evt -> {
                drawDonLeft = false;
                repaint();
                ((Timer) evt.getSource()).stop();
            }).start();
        } else if (keyCode == KeyEvent.VK_K) {
            drawDonRight = true;
            repaint();
            // starts a swing timer for 100ms to display the left right don, then turns it off and repaints
            new Timer(duration, evt -> {
                drawDonRight = false;
                repaint();
                ((Timer) evt.getSource()).stop();
            }).start();
        } else if (keyCode == KeyEvent.VK_A) {
            drawKaLeft = true;
            repaint();
            // starts a swing timer for 100ms to display the left side ka, then turns it off and repaints
            new Timer(duration, evt -> {
                drawKaLeft = false;
                repaint();
                ((Timer) evt.getSource()).stop();
            }).start();
        } else if (keyCode == KeyEvent.VK_L) {
            drawKaRight = true;
            repaint();
            // starts a swing timer for 100ms to display the right side ka, then turns it off and repaints
            new Timer(duration, evt -> {
                drawKaRight = false;
                repaint();
                ((Timer) evt.getSource()).stop();
            }).start();
        }
    }

    public void drawGameboard(Graphics g) {
        // the background
        g.drawImage(InGameBackground, 0, 0, getWidth(), getHeight(), this);
        g.drawImage(Lane, laneLeftX, 120, 2100, 165, this);

        // drum positions
        int drumX = laneLeftX;
        int drumY = 120;
        g.drawImage(TaikoDrum, drumX, drumY, this);
        int drumW = TaikoDrum.getWidth(null);
        int drumH = TaikoDrum.getHeight(null);

        // drum overlays (sx2 constructer is filled too in order to cut off part of the image
        if (drawDonLeft) {
            g.drawImage(DonDrum,
                    drumX, drumY,
                    drumX + drumW / 2, drumY + drumH,
                    0, 0, drumW / 2, drumH,
                    this
            );
        }
        if (drawDonRight) {
            g.drawImage(DonDrum,
                    drumX + drumW / 2, drumY,
                    drumX + drumW, drumY + drumH,
                    drumW / 2, 0, drumW, drumH,
                    this
            );
        }
        if (drawKaLeft) {
            g.drawImage(KaDrum,
                    drumX, drumY,
                    drumX + drumW / 2, drumY + drumH,
                    0, 0, drumW / 2, drumH,
                    this
            );
        }
        if (drawKaRight) {
            g.drawImage(KaDrum,
                    drumX + drumW / 2, drumY,
                    drumX + drumW, drumY + drumH,
                    drumW / 2, 0, drumW, drumH,
                    this
            );
        }

        // Song title
        g.setColor(Color.WHITE);
        g.setFont(new Font("Meiryo", Font.BOLD, 40));
        if (songName.length() > 4) {
            g.drawString(songName.substring(0, songName.length() - 4), 80, 50);
        }

        // draw notes
        for (Note note: notes) {
            note.draw(g, this);
        }

        // Judge circle and feedback meter
        int noteSize = 100;
        int targetX = laneLeftX + hitOffsetX;
        int judgeX = targetX + noteSize / 2;
        int judgeY = 150 + noteSize / 2;

        g.setColor(Color.YELLOW);

        // this was all trial and error
        g.drawOval(judgeX - judgeRadius, judgeY - judgeRadius,
                judgeRadius * 2, judgeRadius * 2);
        // draw inner circle transparantly (alpha value up
        g.setColor(new Color(255, 255, 0, 50));
        g.fillOval(judgeX - judgeRadius, judgeY - judgeRadius,
                judgeRadius * 2, judgeRadius * 2);

        // draw judgement image (good/bad/ok) AND scores
        if (currentJudgmentImage != null) {
            // calculate how much time has passed since the judgment was displayed
            long elapsed = System.currentTimeMillis() - judgmentDisplayTime;
            if (elapsed < judgmentDisplayDuration) {
                //draws the image on top of the judgement image
                g.drawImage(currentJudgmentImage,
                        judgeX - 42, judgeY - 50 - 100,
                        84, 80,
                        this
                );
            }
        }
        drawScores(g);
    }

    // draws scores of the current game.
    private void drawScores(Graphics g) {
        g.setColor(Color.white);
        g.drawImage(JudgeMeter, 0, 400, this);
        g.setFont(new Font("Arial", Font.BOLD, 40));
        g.drawString("" + countGood, 200, 570);
        g.drawString("" + countOK, 200, 610);
        g.drawString("" + countBad, 200, 650);
        g.setColor(Color.black);
        g.fillRoundRect(20, 650, 300, 50, 10, 10);
        g.setColor(Color.white);
        g.drawString("コンボ", 40, 690);
        g.drawString("" + comboScore, 200, 690);
    }

    // draws the end screen and does other misc. ending things such as zeroing the combo and score counters. also stops player.
    public void drawEndScreen(Graphics g) {
        mp3Player.stop();
        gameScore = (countGood * 960) + (countOK * 420);
        final int finalOK = countOK;
        final int finalBad = countBad;
        final int finalGood = countGood;
        final int finalGameScore = gameScore;
        g.drawImage(BackgroundFinished, 0, 0, getWidth(), getHeight(), this);
        g.drawImage(JudgeMeter, 400, 400, this);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 50));
        g.drawString("Song Finished!", getWidth() / 2 - 150, getHeight() / 2);
        g.setFont(new Font("Arial", Font.BOLD, 40));
        g.drawString("" + finalGood, 520, 570);
        g.drawString("" + finalOK, 520, 610);
        g.drawString("" + finalBad, 520, 650);
        g.drawString("" + finalGameScore, 520, 700);
        g.setColor(Color.BLACK);
        g.setColor(Color.white);
        g.drawString("Total Score: ", 290, 700);
        g.drawString("ESCAPE: Exit \n ENTER: New Game", 290, 750);
        gameScore = 0;
        countGood = 0;
        countBad = 0;
        countOK = 0;
        comboScore = 0;

    }

    @Override public void keyTyped(KeyEvent e) {}
    // is this jank? yes. does it work? yes.
    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_S: sHeld = false; break;
            case KeyEvent.VK_K: kHeld = false; break;
            case KeyEvent.VK_A: aHeld = false; break;
            case KeyEvent.VK_L: lHeld = false; break;
            default: break;
        }
    }
}