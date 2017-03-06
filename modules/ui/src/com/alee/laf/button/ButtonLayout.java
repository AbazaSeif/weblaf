/*
 * This file is part of WebLookAndFeel library.
 *
 * WebLookAndFeel library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * WebLookAndFeel library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with WebLookAndFeel library.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.alee.laf.button;

import com.alee.painter.decoration.IDecoration;
import com.alee.painter.decoration.layout.IconTextLayout;
import com.thoughtworks.xstream.annotations.XStreamAlias;

import javax.swing.*;

/**
 * Button icon and text layout implementation.
 *
 * @param <E> component type
 * @param <D> decoration type
 * @param <I> layout type
 * @author Mikle Garin
 */

@XStreamAlias ( "ButtonLayout" )
public class ButtonLayout<E extends AbstractButton, D extends IDecoration<E, D>, I extends ButtonLayout<E, D, I>>
        extends IconTextLayout<E, D, I>
{
    @Override
    protected int getIconTextGap ( final E c, final D d )
    {
        return gap != null ? gap : c.getIconTextGap ();
    }

    @Override
    protected int getHorizontalAlignment ( final E c, final D d )
    {
        return halign != null ? halign.getValue () : c.getHorizontalAlignment ();
    }

    @Override
    protected int getVerticalAlignment ( final E c, final D d )
    {
        return valign != null ? valign.getValue () : c.getVerticalAlignment ();
    }

    @Override
    protected int getHorizontalTextPosition ( final E c, final D d )
    {
        return hpos != null ? hpos.getValue () : c.getHorizontalTextPosition ();
    }

    @Override
    protected int getVerticalTextPosition ( final E c, final D d )
    {
        return vpos != null ? vpos.getValue () : c.getVerticalTextPosition ();
    }
}