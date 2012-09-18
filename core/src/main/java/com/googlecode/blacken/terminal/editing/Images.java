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

package com.googlecode.blacken.terminal.editing;

import com.googlecode.blacken.grid.Grid;
import com.googlecode.blacken.terminal.TerminalViewInterface;
import java.util.Map;

/**
 *
 * @author Steven Black
 */
public class Images {
    public static void imageToBackground(TerminalViewInterface term, int yo, int xo, Grid<Integer> grid, Integer indexStart) {
        for (int y = 0; y < grid.getHeight(); y++) {
            if (y + yo >= term.getHeight()) {
                break;
            }
            for (int x = 0; x < grid.getWidth(); x++) {
                if (x + xo >= term.getWidth()) {
                    break;
                }
                Integer v = grid.get(y, x);
                if (v == null) {
                    continue;
                }
                if (indexStart != null) {
                    v -= indexStart;
                }
                term.get(y + yo, x + xo).setBackground(v);
            }
        }
    }

    public static void imageToForeground(TerminalViewInterface term, int yo, int xo, Grid<Integer> grid, Integer indexStart) {
        for (int y = 0; y < grid.getHeight(); y++) {
            if (y + yo >= term.getHeight()) {
                break;
            }
            for (int x = 0; x < grid.getWidth(); x++) {
                if (x + xo >= term.getWidth()) {
                    break;
                }
                Integer v = grid.get(y, x);
                if (v == null) {
                    continue;
                }
                if (indexStart != null) {
                    v -= indexStart;
                }
                term.get(y + yo, x + xo).setForeground(v);
            }
        }
    }

    public static void imageToSequence(TerminalViewInterface term, int yo, int xo, Grid<Integer> grid,
            Map<Integer, String> translate) {
        for (int y = 0; y < grid.getHeight(); y++) {
            if (y + yo >= term.getHeight()) {
                break;
            }
            for (int x = 0; x < grid.getWidth(); x++) {
                if (x + xo >= term.getWidth()) {
                    break;
                }
                Integer v = grid.get(y, x);
                if (v == null) {
                    continue;
                }
                if (translate != null) {
                    String w = translate.get(v);
                    if (w == null) {
                        continue;
                    }
                    term.get(y + yo, x + xo).setSequence(w);
                } else {
                    term.get(y + yo, x + xo).setSequence(v);
                }
            }
        }
    }

}
