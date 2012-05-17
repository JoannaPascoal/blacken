/* blacken - a library for Roguelike games
 * Copyright © 2010-2012 Steven Black <yam655@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * and the GNU Lesser General Public License along with this program.  
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.googlecode.blacken.exceptions;

/**
 * A simple character was found where a complex was required.
 * 
 * @author yam655
 */
public class NotComplexCharacterException extends Exception {

    private static final long serialVersionUID = 3051266593008304578L;

    /**
     * Failure to find a complex character sequence.
     */
    public NotComplexCharacterException() {
        super();
    }

    /**
     * Failure to find a complex character sequence.
     * @param message descriptive message
     */
    public NotComplexCharacterException(String message) {
        super(message);
    }

    /**
     * Failure to find a complex character sequence.
     * @param cause the cause
     */
    public NotComplexCharacterException(Throwable cause) {
        super(cause);
    }

    /**
     * Failure to find a complex character sequence.
     * @param message descriptive message
     * @param cause the cause
     */
    public NotComplexCharacterException(String message, Throwable cause) {
        super(message, cause);
    }

}
