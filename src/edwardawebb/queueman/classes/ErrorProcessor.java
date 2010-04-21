/**
 *     This file is part of QueueMan.
 *
 *	  QueueMan is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    any later version.
 *
 *    QueueMan is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with QueueMan.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package edwardawebb.queueman.classes;

import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author eddie
 *
 */

public class ErrorProcessor extends DefaultHandler
{

    public ErrorProcessor ()
    {
	    super();
    }

    public void error (SAXParseException e) {
        System.out.println("Error: "+e.getMessage());
    }

    public void fatalError (SAXParseException e) {
        System.out.println("Fatal Error: "+e.getMessage());
    }

    public void warning (SAXParseException e) {
        System.out.println("Warning: "+e.getMessage());
    }

}
