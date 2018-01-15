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

package com.alee.laf.tree;

import com.alee.api.jdk.Consumer;
import com.alee.extended.tree.WebCheckBoxTree;
import com.alee.laf.WebLookAndFeel;
import com.alee.laf.tree.behavior.TreePathHoverBehavior;
import com.alee.managers.icon.Icons;
import com.alee.managers.style.*;
import com.alee.painter.DefaultPainter;
import com.alee.painter.Painter;
import com.alee.painter.PainterSupport;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Enumeration;

/**
 * Custom UI for {@link JTree} component.
 *
 * @author Mikle Garin
 */

public class WebTreeUI extends WTreeUI implements ShapeSupport, MarginSupport, PaddingSupport
{
    /**
     * Default node icons.
     * todo Replace with SVG icons and move into manager for lazy-initialization
     */
    public static ImageIcon ROOT_ICON = new ImageIcon ( WebTreeUI.class.getResource ( "icons/root.png" ) );
    public static ImageIcon CLOSED_ICON = new ImageIcon ( WebTreeUI.class.getResource ( "icons/closed.png" ) );
    public static ImageIcon OPEN_ICON = new ImageIcon ( WebTreeUI.class.getResource ( "icons/open.png" ) );
    public static ImageIcon LEAF_ICON = new ImageIcon ( WebTreeUI.class.getResource ( "icons/leaf.png" ) );

    /**
     * Style settings.
     */
    protected TreeSelectionStyle selectionStyle;

    /**
     * Component painter.
     */
    @DefaultPainter ( TreePainter.class )
    protected ITreePainter painter;

    /**
     * Listeners.
     */
    protected transient TreePathHoverBehavior hoverNodeTracker;

    /**
     * Runtime variables.
     */
    protected transient int hoverRow = -1;

    /**
     * Returns an instance of the {@link WebTreeUI} for the specified component.
     * This tricky method is used by {@link UIManager} to create component UIs when needed.
     *
     * @param c component that will use UI instance
     * @return instance of the {@link WebTreeUI}
     */
    @SuppressWarnings ( "UnusedParameters" )
    public static ComponentUI createUI ( final JComponent c )
    {
        return new WebTreeUI ();
    }

    @Override
    public void installUI ( final JComponent c )
    {
        // Installing UI
        super.installUI ( c );

        // todo Probably completely remove this?
        // Overwrite indent in case WebLookAndFeel is not installed as LaF
        if ( !WebLookAndFeel.isInstalled () )
        {
            setRightChildIndent ( 12 );
            setLeftChildIndent ( 12 );
        }

        // Allow each cell to choose its own preferred height
        tree.setRowHeight ( -1 );

        // Modifying default drop mode
        // USE_SELECTION mode is not preferred since WebLaF provides a better visual drop representation
        tree.setDropMode ( DropMode.ON );

        // Use a moderate amount of visible rows by default
        // BasicTreeUI uses 20 rows by default which is too much for most of cases
        tree.setVisibleRowCount ( 10 );

        // Forces tree to save changes when another tree node is selected instead of cancelling them
        tree.setInvokesStopCellEditing ( true );

        // Hover behavior
        hoverNodeTracker = new TreePathHoverBehavior<JTree> ( tree, true )
        {
            @Override
            public void hoverChanged ( final TreePath previous, final TreePath current )
            {
                // Updating hover row
                final int previousRow = hoverRow;
                hoverRow = current != null ? tree.getRowForPath ( current ) : -1;

                // Repainting nodes according to hover changes
                // This occurs only if hover highlight is enabled
                if ( painter != null && painter.isRowHoverDecorationSupported () )
                {
                    repaintRow ( previousRow );
                    repaintRow ( hoverRow );
                }

                // Updating custom WebLaF tooltip display state
                final TreeToolTipProvider provider = getToolTipProvider ();
                if ( provider != null )
                {
                    provider.hoverAreaChanged ( tree, new TreeCellArea ( previousRow ), new TreeCellArea ( hoverRow ) );
                }

                // Informing {@link com.alee.laf.tree.WebTree} about hover node change
                // This is performed here to avoid excessive listeners usage for the same purpose
                if ( tree instanceof WebTree )
                {
                    final DefaultMutableTreeNode p = previous != null ? ( DefaultMutableTreeNode ) previous.getLastPathComponent () : null;
                    final DefaultMutableTreeNode c = current != null ? ( DefaultMutableTreeNode ) current.getLastPathComponent () : null;
                    ( ( WebTree ) tree ).fireHoverChanged ( p, c );
                }
            }

            /**
             * Repaints specified row if it exists and it is visible.
             *
             * @param row row to repaint
             */
            private void repaintRow ( final int row )
            {
                if ( row != -1 )
                {
                    final Rectangle rowBounds = getRowBounds ( row, true );
                    if ( rowBounds != null )
                    {
                        tree.repaint ( rowBounds );
                    }
                }
            }
        };
        hoverNodeTracker.install ();

        // Applying skin
        StyleManager.installSkin ( tree );
    }

    @Override
    public void uninstallUI ( final JComponent c )
    {
        // Uninstalling applied skin
        StyleManager.uninstallSkin ( tree );

        // Removing custom listeners
        hoverNodeTracker.uninstall ();
        hoverNodeTracker = null;

        // Uninstalling UI
        super.uninstallUI ( c );
    }

    @Override
    public Shape getShape ()
    {
        return PainterSupport.getShape ( tree, painter );
    }

    @Override
    public Insets getMargin ()
    {
        return PainterSupport.getMargin ( tree );
    }

    @Override
    public void setMargin ( final Insets margin )
    {
        PainterSupport.setMargin ( tree, margin );
    }

    @Override
    public Insets getPadding ()
    {
        return PainterSupport.getPadding ( tree );
    }

    @Override
    public void setPadding ( final Insets padding )
    {
        PainterSupport.setPadding ( tree, padding );
    }

    /**
     * Returns tree painter.
     *
     * @return tree painter
     */
    public Painter getPainter ()
    {
        return PainterSupport.getPainter ( painter );
    }

    /**
     * Sets tree painter.
     * Pass null to remove tree painter.
     *
     * @param painter new tree painter
     */
    public void setPainter ( final Painter painter )
    {
        PainterSupport.setPainter ( tree, new Consumer<ITreePainter> ()
        {
            @Override
            public void accept ( final ITreePainter newPainter )
            {
                WebTreeUI.this.painter = newPainter;
            }
        }, this.painter, painter, ITreePainter.class, AdaptiveTreePainter.class );
    }

    @Override
    public int getHoverRow ()
    {
        return hoverRow;
    }

    @Override
    public int getExactRowForLocation ( final Point location )
    {
        return getExactRowForLocation ( location, isFullLineSelection () );
    }

    @Override
    public int getExactRowForLocation ( final Point location, final boolean fullRow )
    {
        if ( tree != null )
        {
            final Enumeration<TreePath> visiblePaths = getVisiblePaths ();
            if ( visiblePaths != null )
            {
                while ( visiblePaths.hasMoreElements () )
                {
                    final TreePath treePath = visiblePaths.nextElement ();
                    final Rectangle bounds = getPathBounds ( treePath, fullRow );
                    if ( bounds != null && bounds.contains ( location ) )
                    {
                        return getRowForPath ( tree, treePath );
                    }
                }
            }
        }
        return -1;
    }

    @Override
    public Rectangle getRowBounds ( final int row )
    {
        return getRowBounds ( row, isFullLineSelection () );
    }

    @Override
    public Rectangle getRowBounds ( final int row, final boolean fullRow )
    {
        final TreePath path = getPathForRow ( tree, row );
        return fullRow ? getFullPathBounds ( path ) : getPathBounds ( tree, path );
    }

    /**
     * Returns full path bounds.
     *
     * @param path    tree path
     * @param fullRow whether take the whole row into account or just node renderer rect
     * @return full path bounds
     */
    public Rectangle getPathBounds ( final TreePath path, final boolean fullRow )
    {
        return fullRow ? getFullPathBounds ( path ) : getPathBounds ( tree, path );
    }

    /**
     * Returns full path bounds.
     *
     * @param path tree path
     * @return full path bounds
     */
    private Rectangle getFullPathBounds ( final TreePath path )
    {
        final Rectangle b = getPathBounds ( tree, path );
        if ( b != null )
        {
            final Insets insets = tree.getInsets ();
            b.x = insets.left;
            b.width = tree.getWidth () - insets.left - insets.right;
        }
        return b;
    }

    /**
     * Returns visible paths enumeration.
     * This is just a small method for convenient enumeration retrieval.
     *
     * @return visible paths enumeration
     */
    public Enumeration<TreePath> getVisiblePaths ()
    {
        if ( tree.isShowing () )
        {
            final Rectangle paintBounds = tree.getVisibleRect ();
            final TreePath initialPath = getClosestPathForLocation ( tree, 0, paintBounds.y );
            if ( initialPath != null )
            {
                return treeState.getVisiblePathsFrom ( initialPath );
            }
        }
        return null;
    }

    @Override
    protected TreeCellEditor createDefaultCellEditor ()
    {
        return new WebTreeCellEditor ();
    }

    @Override
    protected TreeCellRenderer createDefaultCellRenderer ()
    {
        return new WebTreeCellRenderer ();
    }

    @Override
    protected void selectPathForEvent ( final TreePath path, final MouseEvent e )
    {
        if ( !isLocationInCheckBoxControl ( path, e.getX (), e.getY () ) )
        {
            super.selectPathForEvent ( path, e );
        }
    }

    @Override
    public boolean isLocationInCheckBoxControl ( final TreePath path, final int x, final int y )
    {
        if ( tree instanceof WebCheckBoxTree )
        {
            final WebCheckBoxTree checkBoxTree = ( WebCheckBoxTree ) tree;
            if ( checkBoxTree.isCheckingByUserEnabled () )
            {
                final DefaultMutableTreeNode node = ( DefaultMutableTreeNode ) path.getLastPathComponent ();
                if ( checkBoxTree.isCheckBoxVisible ( node ) && checkBoxTree.isCheckBoxEnabled ( node ) )
                {
                    final Rectangle checkBoxBounds = checkBoxTree.getCheckBoxBounds ( path );
                    return checkBoxBounds != null && checkBoxBounds.contains ( x, y );
                }
                else
                {
                    return false;
                }
            }
            else
            {
                return false;
            }
        }
        else
        {
            return false;
        }
    }

    @Override
    public Icon getExpandedIcon ()
    {
        return tree.isEnabled () ? Icons.squareMinus : Icons.squareMinusDisabled;
    }

    @Override
    public Icon getCollapsedIcon ()
    {
        return tree.isEnabled () ? Icons.squarePlus : Icons.squarePlusDisabled;
    }

    @Override
    public CellRendererPane getCellRendererPane ()
    {
        return rendererPane;
    }

    @Override
    public AbstractLayoutCache getTreeLayoutCache ()
    {
        return treeState;
    }

    @Override
    public TreeSelectionStyle getSelectionStyle ()
    {
        return selectionStyle;
    }

    @Override
    public void setSelectionStyle ( final TreeSelectionStyle style )
    {
        this.selectionStyle = style;
    }

    /**
     * Returns whether tree selection style points that the whole line is a single cell or not.
     *
     * @return true if tree selection style points that the whole line is a single cell, false otherwise
     */
    protected boolean isFullLineSelection ()
    {
        return selectionStyle == TreeSelectionStyle.line;
    }

    /**
     * Returns {@link TreeToolTipProvider} for {@link JTree} that uses this {@link WebTreeUI}.
     *
     * @return {@link TreeToolTipProvider} for {@link JTree} that uses this {@link WebTreeUI}
     */
    protected TreeToolTipProvider getToolTipProvider ()
    {
        return tree instanceof WebTree ? ( ( WebTree ) tree ).getToolTipProvider () : null;
    }

    @Override
    public void paint ( final Graphics g, final JComponent c )
    {
        if ( painter != null )
        {
            painter.prepareToPaint ( drawingCache, currentCellRenderer );
            painter.paint ( ( Graphics2D ) g, c, this, new Bounds ( c ) );
        }
    }

    @Override
    public Dimension getPreferredSize ( final JComponent c )
    {
        return PainterSupport.getPreferredSize ( c, super.getPreferredSize ( c ), painter );
    }
}