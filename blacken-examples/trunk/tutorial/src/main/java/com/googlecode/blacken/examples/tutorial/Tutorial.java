/* blacken - a library for Roguelike games
 * Copyright © 2012 Steven Black <yam655@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/
package com.googlecode.blacken.examples.tutorial;

import com.googlecode.blacken.bsp.BSPTree;
import com.googlecode.blacken.colors.ColorNames;
import com.googlecode.blacken.colors.ColorPalette;
import com.googlecode.blacken.core.Random;
import com.googlecode.blacken.dungeon.Room;
import com.googlecode.blacken.dungeon.SimpleDigger;
import com.googlecode.blacken.grid.Grid;
import com.googlecode.blacken.grid.Point;
import com.googlecode.blacken.grid.Positionable;
import com.googlecode.blacken.swing.SwingTerminal;
import com.googlecode.blacken.terminal.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The official tutorial example.
 *
 * <p>This class is listed as the "mainClass" in the pom.xml file. It needs
 * to contain the primary main function. It should also bind the rest of the
 * logic either directly or indirectly together.
 *
 * @author Steven Black
 */
public class Tutorial {

    /**
     * Using a logger allows us to get debug output in a flexible manner which
     * does not otherwise interrupt the flow of the game. Modern loggers for
     * Java support changing logging settings while a program is running,
     * allowing you to set up complex situations without any logging, then turn
     * the debug logging on full-bore to debug a particular problem.
     *
     * <p>Blacken uses SLF4J, which is what we're using here. SLF4J allows us
     * to change the underlying logging infrastructure -- if we want -- without
     * changing any of the actual code. This tutorial is using Log4J for logging,
     * just like Blacken.
     *
     * <p>If you wanted something different, for this tutorial it is all in the
     * pom.xml file (no actual source changes needed). If you wanted to change
     * it for Blacken itself, you could swap JARs (provided they're the same
     * version) without recompiling it.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(Tutorial.class);
    /**
     * The TerminalInterface is the generic interface to the user interface.
     *
     * <p>Currently, we only support a single backend, but that will change.
     * By wrapping the backend, we significantly increase the chances that we
     * will be able to add a new backend without impacting game code.
     */
    protected TerminalInterface term;
    /**
     * Whether to quit the loop or not
     */
    protected boolean quit;
    private Grid<Integer> grid;
    private Random rand;
    // private final static Positionable MAP_START = new Point(1, 0);
    // private final static Positionable MAP_END = new Point(-1, 0);
    private Positionable upperLeft = new Point(0, 0);
    private Player player = new Player();
    private Integer underPlayer = -1;
    private boolean dirtyMsg = false;
    private boolean dirtyStatus = false;
    private String message;
    private float noisePlane;
    private Map<String, Integer> config;
    private Set<Integer> passable;
    private Set<Integer> roomWalls;
    private List<Map<Integer, Representation>> representations = new ArrayList<>();
    private static final int BASE_WIDTH = 80;
    private static final int BASE_HEIGHT = 25;

    public void addRepresentations() {
        // default
        Representation e;
        Map<Integer, Representation> r;

        r = new HashMap<>();
        representations.add(r);

        e = new Representation();
        e.add(config.get("player"), 0xe4);
        r.put(config.get("player"), e);

        e = new Representation();
        e.add(config.get("room:door"), 58, 130, 94, 94, 94, 94, 94, 94, 94, 94);
        r.put(config.get("room:door"), e);

        e = new Representation();
        e.add(config.get("floor"), 0xee, 10);
        r.put(config.get("floor"), e);

        e = new Representation();
        e.add(config.get("hall:floor"), 0xee, 10);
        r.put(config.get("hall:floor"), e);

        e = new Representation();
        e.add(config.get("diggable"), 0x58, 14);
        r.put(config.get("diggable"), e);

        e = new Representation();
        e.add(config.get("hall:wall"), 0x58, 14);
        r.put(config.get("hall:wall"), e);

        for (Integer roomWall : roomWalls) {
            e = new Representation();
            e.add(roomWall, 0x58, 14);
            r.put(roomWall, e);
        }

        e = new Representation();
        e.add(config.get("water"), 17, 11);
        e.add(config.get("mountains"), 236, 20);
        e.add(config.get("water"), 17, 11);
        e.add(config.get("water"), 17, 11);
        e.add(config.get("water"), 17, 11);
        e.add(config.get("mountains"), 236, 20);
        r.put(config.get("void"), e);

        for (char goal='0'; goal <= '9'; goal++) {
            Integer g = new Integer(goal);
            e = new Representation();
            e.add(g, 0x4 + g - '0');
            r.put(g, e);
        }
        
        // nethack

        r = new HashMap<>();
        representations.add(r);
        e = new Representation();
        e.add("@".codePointAt(0), 7);
        r.put(config.get("player"), e);

        e = new Representation();
        e.add("+".codePointAt(0), 7);
        r.put(config.get("room:door"), e);

        e = new Representation();
        e.add(".".codePointAt(0), 7);
        r.put(config.get("floor"), e);

        e = new Representation();
        e.add("#".codePointAt(0), 7);
        r.put(config.get("hall:floor"), e);

        e = new Representation();
        e.add(" ".codePointAt(0), 0);
        r.put(config.get("diggable"), e);

        e = new Representation();
        e.add(" ".codePointAt(0), 0);
        r.put(config.get("hall:wall"), e);

        e = new Representation();
        e.add("-".codePointAt(0), 7);
        r.put(config.get("room:wall:top"), e);
        r.put(config.get("room:wall:bottom"), e);
        r.put(config.get("room:wall:top-left"), e);
        r.put(config.get("room:wall:top-right"), e);
        r.put(config.get("room:wall:bottom-left"), e);
        r.put(config.get("room:wall:bottom-right"), e);

        e = new Representation();
        e.add("|".codePointAt(0), 7);
        r.put(config.get("room:wall:left"), e);
        r.put(config.get("room:wall:right"), e);

        for (Integer roomWall : roomWalls) {
            if (!r.containsKey(roomWall)) {
                e = new Representation();
                e.add(roomWall, 0x58, 14);
                r.put(roomWall, e);
            }
        }

        e = new Representation();
        e.add(" ".codePointAt(0), 0);
        r.put(config.get("void"), e);

        for (char goal='0'; goal <= '9'; goal++) {
            Integer g = new Integer(goal);
            e = new Representation();
            e.add(g, 0x4 + g - '0');
            r.put(g, e);
        }

        // moria

        r = new HashMap<>();
        representations.add(r);

        e = new Representation();
        e.add((int)'@', 0xe4);
        r.put(config.get("player"), e);

        e = new Representation();
        e.add((int)'+', 58, 130, 94, 94, 94, 94, 94, 94, 94, 94);
        e.add((int)'+', 58, 130, 94, 94, 94, 94, 94, 94, 94, 94);
        e.add((int)'\'', 58, 130, 94, 94, 94, 94, 94, 94, 94, 94);
        r.put(config.get("room:door"), e);

        e = new Representation();
        e.add((int)'.', 0xee, 10);
        r.put(config.get("floor"), e);

        r.put(config.get("hall:floor"), e);

        e = new Representation();
        e.add(BlackenCodePoints.CODEPOINT_MEDIUM_SHADE, 0x58, 14);
        e.add(BlackenCodePoints.CODEPOINT_LIGHT_SHADE, 0x58, 14);
        e.add(BlackenCodePoints.CODEPOINT_MEDIUM_SHADE, 0x58, 14);
        e.add(BlackenCodePoints.CODEPOINT_MEDIUM_SHADE, 0x58, 14);
        r.put(config.get("diggable"), e);

        e = r.get(config.get("diggable"));
        r.put(config.get("hall:wall"), e);

        for (Integer roomWall : roomWalls) {
            e = new Representation();
            e.add(BlackenCodePoints.CODEPOINT_MEDIUM_SHADE, 0x58, 14);
            r.put(roomWall, e);
        }

        e = new Representation();
        e.add(" ".codePointAt(0), 0);
        r.put(config.get("void"), e);

        for (char goal='0'; goal <= '9'; goal++) {
            Integer g = new Integer(goal);
            e = new Representation();
            e.add(g, 0x4 + g - '0');
            r.put(g, e);
        }

    }

    /**
     * Create a new instance
     */
    public Tutorial() {
        rand = new Random();
        noisePlane = rand.nextFloat();
        config = new HashMap<>();
        // Used by Simple Digger
        // Courier New doesn't have Heavy, but does have Double.
        config.put("diggable", "\u2592".codePointAt(0)); // 50% shade
        config.put("floor", "\u25AA".codePointAt(0)); // small black square
        config.put("hall:floor", "\u25AB".codePointAt(0)); // sm. white square
        config.put("hall:wall", "\u2591".codePointAt(0)); // 25% shade
        config.put("room:door", "+".codePointAt(0));
        config.put("room:wall:top", "\u2500".codePointAt(0)); // light horiz
        config.put("room:wall:left", "\u2502".codePointAt(0)); // light vert
        config.put("room:wall:bottom", "\u2550".codePointAt(0)); // heavy horiz
        config.put("room:wall:right", "\u2551".codePointAt(0)); // heavy horiz
        config.put("room:wall:top-left", "\u250C".codePointAt(0)); // Lh/Lv
        config.put("room:wall:top-right", "\u2556".codePointAt(0)); // Lh/Hv
        config.put("room:wall:bottom-left", "\u2558".codePointAt(0)); // Hh/Lv
        config.put("room:wall:bottom-right", "\u255D".codePointAt(0)); // Hv/Hh
        
        // game specific
        config.put("void", " ".codePointAt(0));
        config.put("player", "@".codePointAt(0));
        config.put("water", "~".codePointAt(0));
        config.put("mountains", "^".codePointAt(0));

        grid = new Grid<>(config.get("diggable"), 100, 100);
        passable = new HashSet<>();
        passable.add(config.get("floor"));
        passable.add(config.get("hall:floor"));
        passable.add(config.get("room:door"));

        roomWalls = new HashSet<>();
        // roomWalls.add(config.get("room:wall"));
        roomWalls.add(config.get("room:wall:top"));
        roomWalls.add(config.get("room:wall:left"));
        roomWalls.add(config.get("room:wall:bottom"));
        roomWalls.add(config.get("room:wall:right"));
        roomWalls.add(config.get("room:wall:top-left"));
        roomWalls.add(config.get("room:wall:top-right"));
        roomWalls.add(config.get("room:wall:bottom-left"));
        roomWalls.add(config.get("room:wall:bottom-right"));

    }


    /**
     * Make a map
     */
    private void makeMap() {
        grid.clear();
        SimpleDigger simpleDigger = new SimpleDigger();
        BSPTree<Room> bsp = simpleDigger.setup(grid, config);
        List<Room> rooms = new ArrayList(bsp.findContained(null));
        Collections.shuffle(rooms, rand);
        int idx = 0;
        for (Integer c = 0x31; c < 0x3a; c++) {
            rooms.get(idx).assignToContainer(c);
            idx++;
            if (idx >= rooms.size()) {
                idx = 0;
                Collections.shuffle(rooms, rand);
            }
        }
        // simpleDigger.digRoomAvoidanceHalls(bsp, grid, config);
        simpleDigger.digHallFirst(bsp, grid, config, false);
        underPlayer = config.get("room:floor");
        Positionable pos = rooms.get(idx).placeThing(grid, underPlayer, config.get("player"));
        this.player.setPosition(pos);
        recenterMap();
    }

    /*
    private void showMap() {
        int ey = MAP_END.getY();
        int ex = MAP_END.getX();
        if (ey <= 0) {
            ey += term.getHeight();
        }
        if (ex <= 0) {
            ex += term.getWidth();
        }
        Map<Integer, Representation> currentRep = this.representations.get(this.represent);
        for (int y = MAP_START.getY(); y < ey; y++) {
            for (int x = MAP_START.getX(); x < ex; x++) {
                int y1 = y + upperLeft.getY() - MAP_START.getY();
                int x1 = x + upperLeft.getX() - MAP_START.getX();
                int what = config.get("void");
                if (y1 >= 0 && x1 >= 0 && y1 < grid.getHeight() && x1 < grid.getWidth()) {
                    what = grid.get(y1, x1);
                }
                Representation how = currentRep.get(what);
                if (how == null) {
                    LOGGER.error("Failed to find entry for {}", BlackenKeys.toString(what));
                }
                double noise = PerlinNoise.noise(x1, y1, noisePlane);
                int as = how.getCodePoint(noise);
                int fclr = how.getColor(noise);
                int bclr = 0;
                EnumSet<CellWalls> walls = EnumSet.noneOf(CellWalls.class);
                if (what >= '0' && what <= '9') {
                    if (what > nextLocation) {
                        walls = CellWalls.BOX;
                    }
                }
                term.set(y, x, BlackenCodePoints.asString(as),
                         fclr, bclr, EnumSet.noneOf(TerminalStyle.class), walls);
            }
        }
    }
    */

    /**
     * The application loop.
     * @return the quit status
     */
    public boolean loop() {
        /*
        makeMap();
        term.disableEventNotices();
        int ch = BlackenKeys.NO_KEY;
        int mod;
        updateStatus();
        movePlayerBy(0,0);
        this.message = "Welcome to a Swamp Orc Adventure!";
        term.move(-1, -1);
        while (!quit) {
            if (dirtyStatus) {
                updateStatus();
            }
            updateMessage(false);
            showMap();
            term.setCursorLocation(player.getY() - upperLeft.getY() + MAP_START.getY(), 
                                   player.getX() - upperLeft.getX() + MAP_START.getX());
            this.term.getPalette().rotate(0xee, 10, +1);
            // term.refresh();
            mod = BlackenKeys.NO_KEY;
            ch = term.getch();
            if (ch == BlackenKeys.RESIZE_EVENT) {
                this.refreshScreen();
                continue;
            } else if (BlackenKeys.isModifier(ch)) {
                mod = ch;
                ch = term.getch();
            }
            // LOGGER.debug("Processing key: {}", ch);
            if (ch != BlackenKeys.NO_KEY) {
                this.message = null;
                doAction(mod, ch);
            }
        }
        */
        return this.quit;
    }

    private void updateMessage(boolean press) {
        if (this.message != null && !dirtyMsg) {
            dirtyMsg = true;
        }
        if (dirtyMsg) {
            /*
            for (int x = 0; x < term.getWidth(); x++) {
                term.mvaddch(0, x, ' ');
            }
            if (message == null) {
                dirtyMsg = false;
            } else {
                term.mvputs(0, 0, message);
            }
            if (press) {
                message = null;
            }
            */
        }
    }

    /**
     * Update the status.
     */
    private void updateStatus() {
        /*
        term.setCurForeground(7);
        dirtyStatus = false;
        for (int x = 0; x < term.getWidth()-1; x++) {
            term.mvaddch(term.getHeight(), x, ' ');
        }
        if (nextLocation <= '9') {
            term.mvputs(term.getHeight(), 0, "Get the ");
            term.setCurForeground((nextLocation - '0') + 0x4);
            term.addch(nextLocation);
            term.setCurForeground(7);
            if (nextLocation == '9') {
                term.puts(" to win.");
            }
        } else {
            term.mvputs(term.getHeight(), 0, "You won!");
        }
        String msg = "Q to quit.";
        term.mvputs(term.getHeight(), term.getWidth()-msg.length()-1, msg);
        */
    }

    private void redraw() {
        term.clear();
        updateStatus();
        updateMessage(false);
        //this.showMap();
    }
    
    private boolean doAction(int modifier, int ch) {
        if (BlackenModifier.MODIFIER_KEY_CTRL.hasFlag(modifier)) {
            switch (ch) {
            case 'l':
            case 'L':
                this.recenterMap();
                redraw();
                break;
            }
            return false;
        } else {
            switch (ch) {
            case 'j':
            case BlackenKeys.KEY_DOWN:
            case BlackenKeys.KEY_NP_2:
            case BlackenKeys.KEY_KP_DOWN:
                movePlayerBy(+1,  0);
                break;
            case 'k':
            case BlackenKeys.KEY_UP:
            case BlackenKeys.KEY_NP_8:
            case BlackenKeys.KEY_KP_UP:
                movePlayerBy(-1,  0);
                break;
            case 'h':
            case BlackenKeys.KEY_LEFT:
            case BlackenKeys.KEY_NP_4:
            case BlackenKeys.KEY_KP_LEFT:
                movePlayerBy(0,  -1);
                break;
            case 'l':
            case BlackenKeys.KEY_RIGHT:
            case BlackenKeys.KEY_NP_6:
            case BlackenKeys.KEY_KP_RIGHT:
                movePlayerBy(0,  +1);
                break;
            case 'q':
            case 'Q':
            case BlackenKeys.KEY_ESCAPE:
                this.quit = true;
                return false;
            case 'L':
                HelpSystem.blackenLicense(term);
                redraw();
                break;
            case 'N':
                HelpSystem.legalNotices(term);
                redraw();
                break;
            case 'F':
                HelpSystem.fontLicense(term);
                redraw();
                break;
            case '?':
                HelpSystem.help(term);
                redraw();
                break;
            default:
                return false;
            }
        }
        return true;
    }

    /**
     * Move the player by an offset
     * 
     * @param y row offset (0 stationary)
     * @param x column offset (0 stationary)
     */
    private void movePlayerBy(int y, int x) {
        Integer there;
        Positionable oldPos = player.getPosition();
        try {
            there = grid.get(player.getY() + y, player.getX() + x);
        } catch(IndexOutOfBoundsException e) {
            return;
        }
        /*
        if (passable.contains(there) || there == nextLocation) {
            grid.set(oldPos.getY(), oldPos.getX(), underPlayer);
            player.setPosition(player.getY() + y, player.getX() + x);
            underPlayer = grid.get(player);
            grid.set(player.getY(), player.getX(), 0x40);
            int playerScreenY = player.getY() - upperLeft.getY() + MAP_START.getY();  
            int playerScreenX = player.getX() - upperLeft.getX() + MAP_START.getX();
            int ScreenY2 = (MAP_END.getY() <= 0 
                    ? term.getHeight() -1 + MAP_END.getY() : MAP_END.getY());
            int ScreenX2 = (MAP_END.getX() <= 0 
                    ? term.getWidth() -1 + MAP_END.getX() : MAP_END.getX());
            if (playerScreenY >= ScreenY2 || playerScreenX >= ScreenX2 ||
                    playerScreenY <= MAP_START.getY() || 
                    playerScreenX <= MAP_START.getX()) {
                recenterMap();
            }
            if (there == nextLocation) {
                StringBuilder buf = new StringBuilder();
                buf.append("Got it.");
                buf.append(' ');
                if (there == '9') {
                    buf.append("All done!");
                } else {
                    buf.append("Next is unlocked.");
                }
                this.underPlayer = config.get("room:floor");
                nextLocation ++;
                this.message = buf.toString();
                dirtyStatus = true;
                this.updateMessage(false);
            }
        } else if (there >= '0' && there <= '9') {
            this.message = "That position is still locked.";
            this.updateMessage(false);
        }
        */
    }

    private void recenterMap() {
        upperLeft.setY(player.getY() - (term.getHeight()-2)/2);
        upperLeft.setX(player.getX() - (term.getWidth()-2)/2);
    }
    
    
    /**
     * Initialize the example
     * 
     * @param term alternate TerminalInterface to use
     * @param palette alternate ColorPalette to use
     */
    public void init(TerminalInterface term, ColorPalette palette) {
        if (term == null) {
            term = new SwingTerminal();
            term.overrideConfig("Swamp Orc Adventure");
            term.init("Blacken Example: Swamp Orc Adventure", BASE_HEIGHT, BASE_WIDTH);
        }
        this.term = term;
        if (palette == null) {
            palette = new ColorPalette();
            palette.addAll(ColorNames.XTERM_256_COLORS, false);
        }
        this.term.setPalette(palette);
        addRepresentations();
    }
    
    /**
     * Start the example
     * 
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        Tutorial that = new Tutorial();
        that.init(null, null);
        SplashScreen screen = new SplashScreen(that.term);
        screen.run();
        screen.handleResizeEvent();
        Game.setTerminal(that.term);
        MultiPick pick = new MultiPick(that.term, "Please decide carefully.");
        pick.setStaticMessage(
  "The young farmer says, \"The swamp orcs totally devastated my uncle's cabbage\n"
+ "crop.\" The bartender says, \"The Ochre Mage said they're supposed to stay in\n"
+ " the swamp.\" The farmer shrugged, \"He's never said why they're supposed to\n"
+ "stay in the swamp. Maybe they found a way around his magic?\""
+ "\n\n" +
  "The shopkeeper says, \"You mean she.\" Her voice firm, \"The Ochre Mage is a\n"
+ "woman. She deals in orphans and talks shop with nursemaids and those that\n"
+ "assist with the births of man and beast.\""
+ "\n\n" +
  "The young farmer says, \"Man or woman, the Ochre Mage is powerful. The Ochre\n"
+ "Mage also isn't expected to return for several months. We have a swamp orc\n"
+ "problem today.\""
+ "\n\n" +
  "The bartender says, \"Which uncle are you talking about? Old Jim? The one\n"
+ "that goes three towns over to do his drinking so his wife won't find out?\""
+ "\n\n" +
  "The young farmer says, \"It is Old Jim, but he doesn't drink...\""
+ "\n\n" +
  "The bartender laughs, \"My brother and his husband run the bar. Your uncle\n"
+ "is there religiously five nights a week -- you've never wondered why he's so\n"
+ "broke? It's not swamp orcs -- if such creatures even exist -- he's selling\n"
+ "his crop and drinking the profits.\""
+ "\n\n" +
  "The mayor adjusts his tie, and says, \"I think we need to nip this in the\n"
+ "bud. I propose we have a swamp orc killing contest. We can drive some\n"
+ "commerce into town while we're at it.\""
+ "\n\n" +
  "The shopkeeper shrugs, \"It's foolish, but I've got an apprentice that's\n"
+ "been with me three years and hasn't seen a proper busy day.\""
+ "\n\n" +
  "The swamp orc contest starts tomorrow morning. If you leave tonight you can\n"
+ "have a head start.\n\n");
        pick.addChoice("Cheat", "Go to the swamp early to get a head start.");
        pick.addChoice("Balk", "Complain that the Ochre Mage wouldn't like it.");
        pick.setStaticOnTop(true);
        pick.run();
        ConfirmationDialog confirm = new ConfirmationDialog(that.term,
                screen, "Are you sure you want to quit?", "No", "Yes");
        confirm.setColor(0xFFaaaaaa, 0xFF222299);
        confirm.run();
        String got = confirm.getCurrentOptionText();
        LOGGER.error("Confirmation dialog returned: {}", got);
        Game game = Game.getInstance();
        if ("Yes".equals(got)) {
            game.quit();
        }
        game.getGameOver().run();
        that.loop();
        that.quit();
    }
    
    /**
     * Quit the application.
     * 
     * <p>This calls quit on the underlying TerminalInterface.</p>
     */
    public void quit() {
        term.quit();
    }

}
