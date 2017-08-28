package ru.nest.jvg;

import java.awt.geom.AffineTransform;
import java.util.HashMap;
import java.util.Map;

import javax.swing.text.StyleConstants;

import ru.nest.jvg.action.AlignmentAction;
import ru.nest.jvg.action.FontBoldAction;
import ru.nest.jvg.action.FontItalicAction;
import ru.nest.jvg.action.FontStrikeAction;
import ru.nest.jvg.action.FontSubscriptAction;
import ru.nest.jvg.action.FontSuperscriptAction;
import ru.nest.jvg.action.FontUnderlineAction;
import ru.nest.jvg.action.GroupAction;
import ru.nest.jvg.action.GroupAlignmentAction;
import ru.nest.jvg.action.JVGAction;
import ru.nest.jvg.action.OrderAction;
import ru.nest.jvg.action.PaneZoomAction;
import ru.nest.jvg.action.RemoveAction;
import ru.nest.jvg.action.SameSizeAction;
import ru.nest.jvg.action.SameSpacesAction;
import ru.nest.jvg.action.SelectAction;
import ru.nest.jvg.action.TextAlignmentAction;
import ru.nest.jvg.action.TransferAction;
import ru.nest.jvg.action.TransformAction;
import ru.nest.jvg.action.TravelsalAction;
import ru.nest.jvg.shape.text.JVGStyleConstants;

public class JVGEditorKit {
	public JVGEditorKit() {
	}

	public final static String SELECT_FOCUSED_ACTION = "select-focused";

	public final static String SELECT_ALL_ACTION = "select-all";

	public final static String UNSELECT_ALL_ACTION = "unselect-all";

	public final static String[] selectionActions = new String[] { SELECT_FOCUSED_ACTION, SELECT_ALL_ACTION, UNSELECT_ALL_ACTION };

	public final static String TO_FRONT_ACTION = "to-front";

	public final static String TO_UP_ACTION = "to-up";

	public final static String TO_DOWN_ACTION = "to-down";

	public final static String TO_BACK_ACTION = "to-back";

	public final static String[] orderActions = new String[] { TO_FRONT_ACTION, TO_UP_ACTION, TO_DOWN_ACTION, TO_BACK_ACTION };

	public final static String NEXT_ACTION = "next";

	public final static String PREV_ACTION = "prev";

	public final static String[] traversalActions = new String[] { NEXT_ACTION, PREV_ACTION };

	public final static String TOP_ALIGNMENT_ACTION = "top-alignment";

	public final static String LEFT_ALIGNMENT_ACTION = "left-alignment";

	public final static String BOTTOM_ALIGNMENT_ACTION = "bottom-alignment";

	public final static String RIGHT_ALIGNMENT_ACTION = "right-alignment";

	public final static String CENTER_HOR_ALIGNMENT_ACTION = "center-hor-alignment";

	public final static String CENTER_VER_ALIGNMENT_ACTION = "center-ver-alignment";

	public final static String CENTER_ALIGNMENT_ACTION = "center-alignment";

	public final static String[] alignmentActions = new String[] { TOP_ALIGNMENT_ACTION, LEFT_ALIGNMENT_ACTION, BOTTOM_ALIGNMENT_ACTION, RIGHT_ALIGNMENT_ACTION, CENTER_HOR_ALIGNMENT_ACTION, CENTER_VER_ALIGNMENT_ACTION, CENTER_ALIGNMENT_ACTION };

	public final static String TOP_GROUP_ALIGNMENT_ACTION = "top-group-alignment";

	public final static String LEFT_GROUP_ALIGNMENT_ACTION = "left-group-alignment";

	public final static String BOTTOM_GROUP_ALIGNMENT_ACTION = "bottom-group-alignment";

	public final static String RIGHT_GROUP_ALIGNMENT_ACTION = "right-group-alignment";

	public final static String CENTER_HOR_GROUP_ALIGNMENT_ACTION = "center-hor-group-alignment";

	public final static String CENTER_VER_GROUP_ALIGNMENT_ACTION = "center-ver-group-alignment";

	public final static String CENTER_GROUP_ALIGNMENT_ACTION = "center-group-alignment";

	public final static String[] groupAlignmentActions = new String[] { TOP_GROUP_ALIGNMENT_ACTION, LEFT_GROUP_ALIGNMENT_ACTION, BOTTOM_GROUP_ALIGNMENT_ACTION, RIGHT_GROUP_ALIGNMENT_ACTION, CENTER_HOR_GROUP_ALIGNMENT_ACTION, CENTER_VER_GROUP_ALIGNMENT_ACTION, CENTER_GROUP_ALIGNMENT_ACTION };

	public final static String REMOVE_ACTION = "remove";

	public final static String TRANSLATE_UP_ACTION = "translate-up";

	public final static String TRANSLATE_LEFT_ACTION = "translate-left";

	public final static String TRANSLATE_DOWN_ACTION = "translate-down";

	public final static String TRANSLATE_RIGHT_ACTION = "translate-right";

	public final static String ROTATE_90_ACTION = "rotate-90";

	public final static String ROTATE_MINUS_90_ACTION = "rotate-minus-90";

	public final static String ROTATE_180_ACTION = "rotate-180";

	public final static String FLIP_HORIZONTAL_ACTION = "flip-horizontal";

	public final static String FLIP_VERTICAL_ACTION = "flip-vertical";

	public final static String MOVE_UP__TOP_BORDER_ACTION = "move-up--top-border";

	public final static String MOVE_DOWN__TOP_BORDER_ACTION = "move-down--top-border";

	public final static String MOVE_LEFT__LEFT_BORDER_ACTION = "move-left--left-border";

	public final static String MOVE_RIGHT__LEFT_BORDER_ACTION = "move-right--left-border";

	public final static String MOVE_UP__BOTTOM_BORDER_ACTION = "move-up--bottom-border";

	public final static String MOVE_DOWN__BOTTOM_BORDER_ACTION = "move-down--bottom-border";

	public final static String MOVE_LEFT__RIGHT_BORDER_ACTION = "move-left--right-border";

	public final static String MOVE_RIGHT__RIGHT_BORDER_ACTION = "move-right--right-border";

	public final static String SAME_WIDTH_ACTION = "same-width";

	public final static String SAME_HEIGHT_ACTION = "same-height";

	public final static String SAME_SIZE_ACTION = "same-size";

	public final static String[] sameSizeActions = new String[] { SAME_WIDTH_ACTION, SAME_HEIGHT_ACTION, SAME_SIZE_ACTION };

	public final static String SAME_SPACES_HORIZONTAL_ACTION = "same-spaces-horizontal";

	public final static String SAME_SPACES_VERTICAL_ACTION = "same-spaces-vertical";

	public final static String SAME_SPACES_ACTION = "same-spaces";

	public final static String[] sameSpacesActions = new String[] { SAME_SPACES_HORIZONTAL_ACTION, SAME_SPACES_VERTICAL_ACTION, SAME_SPACES_ACTION };

	public final static String GROUP_ACTION = "group-ungroup";

	public final static String CUT_ACTION = "cut";

	public final static String COPY_ACTION = "copy";

	public final static String PASTE_ACTION = "paste";

	// text
	public final static String FONT_BOLD_ACTION = "font-bold";

	public final static String FONT_ITALIC_ACTION = "font-italic";

	public final static String FONT_UNDERLINE_ACTION = "font-underline";

	public final static String FONT_STRIKE_ACTION = "font-strike";

	public final static String FONT_SUBSCRIPT_ACTION = "font-subscript";

	public final static String FONT_SUPERSCRIPT_ACTION = "font-superscript";

	public final static String TEXT_ALIGNMENT_LEFT_ACTION = "text-alignment-left";

	public final static String TEXT_ALIGNMENT_CENTER_ACTION = "text-alignment-center";

	public final static String TEXT_ALIGNMENT_RIGHT_ACTION = "text-alignment-rignt";

	public final static String TEXT_ALIGNMENT_JUSTIFY_ACTION = "text-alignment-justify";

	// path
	public final static String MOVE_TO_ACTION = "move";

	public final static String LINE_TO_ACTION = "append-line";

	public final static String QUAD_TO_ACTION = "append-quad";

	public final static String CUBIC_TO_ACTION = "append-cubic";

	public final static String CLOSE_PATH_ACTION = "close-path";

	public final static String[] appendToPathActions = new String[] { MOVE_TO_ACTION, LINE_TO_ACTION, QUAD_TO_ACTION, CUBIC_TO_ACTION, CLOSE_PATH_ACTION };

	// editor pane
	public final static String PANE_ZOOM_IN = "pane-zoom-in";

	public final static String PANE_ZOOM_OUT = "pane-zoom-out";

	private static Map<String, JVGAction> defaultActions = new HashMap<String, JVGAction>();
	static {
		// select
		defaultActions.put(SELECT_FOCUSED_ACTION, new SelectAction(SelectAction.SELECT_FOCUSED));
		defaultActions.put(SELECT_ALL_ACTION, new SelectAction(SelectAction.SELECT_ALL));
		defaultActions.put(UNSELECT_ALL_ACTION, new SelectAction(SelectAction.UNSELECT_ALL));

		// layer
		defaultActions.put(TO_FRONT_ACTION, new OrderAction(OrderAction.TO_FRONT));
		defaultActions.put(TO_UP_ACTION, new OrderAction(OrderAction.TO_UP));
		defaultActions.put(TO_DOWN_ACTION, new OrderAction(OrderAction.TO_DOWN));
		defaultActions.put(TO_BACK_ACTION, new OrderAction(OrderAction.TO_BACK));

		// focus
		defaultActions.put(NEXT_ACTION, new TravelsalAction(TravelsalAction.NEXT));
		defaultActions.put(PREV_ACTION, new TravelsalAction(TravelsalAction.PREV));

		// alignment
		defaultActions.put(TOP_ALIGNMENT_ACTION, new AlignmentAction(AlignmentAction.TOP));
		defaultActions.put(LEFT_ALIGNMENT_ACTION, new AlignmentAction(AlignmentAction.LEFT));
		defaultActions.put(BOTTOM_ALIGNMENT_ACTION, new AlignmentAction(AlignmentAction.BOTTOM));
		defaultActions.put(RIGHT_ALIGNMENT_ACTION, new AlignmentAction(AlignmentAction.RIGHT));
		defaultActions.put(CENTER_HOR_ALIGNMENT_ACTION, new AlignmentAction(AlignmentAction.CENTER_HOR));
		defaultActions.put(CENTER_VER_ALIGNMENT_ACTION, new AlignmentAction(AlignmentAction.CENTER_VER));
		defaultActions.put(CENTER_ALIGNMENT_ACTION, new AlignmentAction(AlignmentAction.CENTER));

		// group alignment
		defaultActions.put(TOP_GROUP_ALIGNMENT_ACTION, new GroupAlignmentAction(GroupAlignmentAction.TOP));
		defaultActions.put(LEFT_GROUP_ALIGNMENT_ACTION, new GroupAlignmentAction(GroupAlignmentAction.LEFT));
		defaultActions.put(BOTTOM_GROUP_ALIGNMENT_ACTION, new GroupAlignmentAction(GroupAlignmentAction.BOTTOM));
		defaultActions.put(RIGHT_GROUP_ALIGNMENT_ACTION, new GroupAlignmentAction(GroupAlignmentAction.RIGHT));
		defaultActions.put(CENTER_HOR_GROUP_ALIGNMENT_ACTION, new GroupAlignmentAction(GroupAlignmentAction.CENTER_HOR));
		defaultActions.put(CENTER_VER_GROUP_ALIGNMENT_ACTION, new GroupAlignmentAction(GroupAlignmentAction.CENTER_VER));
		defaultActions.put(CENTER_GROUP_ALIGNMENT_ACTION, new GroupAlignmentAction(GroupAlignmentAction.CENTER));

		// remove
		defaultActions.put(REMOVE_ACTION, new RemoveAction());

		// transforms
		defaultActions.put(TRANSLATE_UP_ACTION, new TransformAction(TRANSLATE_UP_ACTION, AffineTransform.getTranslateInstance(0, -1)));
		defaultActions.put(TRANSLATE_LEFT_ACTION, new TransformAction(TRANSLATE_LEFT_ACTION, AffineTransform.getTranslateInstance(-1, 0)));
		defaultActions.put(TRANSLATE_DOWN_ACTION, new TransformAction(TRANSLATE_DOWN_ACTION, AffineTransform.getTranslateInstance(0, 1)));
		defaultActions.put(TRANSLATE_RIGHT_ACTION, new TransformAction(TRANSLATE_RIGHT_ACTION, AffineTransform.getTranslateInstance(1, 0)));

		defaultActions.put(ROTATE_90_ACTION, new TransformAction(ROTATE_90_ACTION, AffineTransform.getRotateInstance(Math.PI / 2.0), 0.5, 0.5));
		defaultActions.put(ROTATE_MINUS_90_ACTION, new TransformAction(ROTATE_MINUS_90_ACTION, AffineTransform.getRotateInstance(-Math.PI / 2.0), 0.5, 0.5));
		defaultActions.put(ROTATE_180_ACTION, new TransformAction(ROTATE_180_ACTION, AffineTransform.getRotateInstance(Math.PI), 0.5, 0.5));

		defaultActions.put(FLIP_HORIZONTAL_ACTION, new TransformAction(FLIP_HORIZONTAL_ACTION, AffineTransform.getScaleInstance(-1, 1), 0.5, 0.5));
		defaultActions.put(FLIP_VERTICAL_ACTION, new TransformAction(FLIP_VERTICAL_ACTION, AffineTransform.getScaleInstance(1, -1), 0.5, 0.5));

		defaultActions.put(MOVE_UP__TOP_BORDER_ACTION, new TransformAction(MOVE_UP__TOP_BORDER_ACTION, 0, 1, 0, 1));
		defaultActions.put(MOVE_DOWN__TOP_BORDER_ACTION, new TransformAction(MOVE_UP__TOP_BORDER_ACTION, 0, 1, 0, -1));
		defaultActions.put(MOVE_UP__BOTTOM_BORDER_ACTION, new TransformAction(MOVE_UP__BOTTOM_BORDER_ACTION, 0, 0, 0, -1));
		defaultActions.put(MOVE_DOWN__BOTTOM_BORDER_ACTION, new TransformAction(MOVE_DOWN__BOTTOM_BORDER_ACTION, 0, 0, 0, 1));
		defaultActions.put(MOVE_LEFT__LEFT_BORDER_ACTION, new TransformAction(MOVE_LEFT__LEFT_BORDER_ACTION, 1, 0, 1, 0));
		defaultActions.put(MOVE_RIGHT__LEFT_BORDER_ACTION, new TransformAction(MOVE_RIGHT__LEFT_BORDER_ACTION, 1, 0, -1, 0));
		defaultActions.put(MOVE_LEFT__RIGHT_BORDER_ACTION, new TransformAction(MOVE_LEFT__RIGHT_BORDER_ACTION, 0, 0, -1, 0));
		defaultActions.put(MOVE_RIGHT__RIGHT_BORDER_ACTION, new TransformAction(MOVE_RIGHT__RIGHT_BORDER_ACTION, 0, 0, 1, 0));

		// same sizes
		defaultActions.put(SAME_WIDTH_ACTION, new SameSizeAction(SameSizeAction.HORIZONTAL));
		defaultActions.put(SAME_HEIGHT_ACTION, new SameSizeAction(SameSizeAction.VERTICAL));
		defaultActions.put(SAME_SIZE_ACTION, new SameSizeAction(SameSizeAction.BOTH));

		// same spaces
		defaultActions.put(SAME_SPACES_HORIZONTAL_ACTION, new SameSpacesAction(SameSpacesAction.HORIZONTAL));
		defaultActions.put(SAME_SPACES_VERTICAL_ACTION, new SameSpacesAction(SameSpacesAction.VERTICAL));
		defaultActions.put(SAME_SPACES_ACTION, new SameSpacesAction(SameSpacesAction.BOTH));

		// group / ungroup
		defaultActions.put(GROUP_ACTION, new GroupAction());

		// transfer: cut / copy / paste
		defaultActions.put(CUT_ACTION, new TransferAction(CUT_ACTION));
		defaultActions.put(COPY_ACTION, new TransferAction(COPY_ACTION));
		defaultActions.put(PASTE_ACTION, new TransferAction(PASTE_ACTION));

		// text
		defaultActions.put(FONT_BOLD_ACTION, new FontBoldAction());
		defaultActions.put(FONT_ITALIC_ACTION, new FontItalicAction());
		defaultActions.put(FONT_UNDERLINE_ACTION, new FontUnderlineAction(JVGStyleConstants.UNDERLINE_LINE));
		defaultActions.put(FONT_STRIKE_ACTION, new FontStrikeAction());
		defaultActions.put(FONT_SUBSCRIPT_ACTION, new FontSubscriptAction());
		defaultActions.put(FONT_SUPERSCRIPT_ACTION, new FontSuperscriptAction());
		defaultActions.put(TEXT_ALIGNMENT_LEFT_ACTION, new TextAlignmentAction(TEXT_ALIGNMENT_LEFT_ACTION, StyleConstants.ALIGN_LEFT));
		defaultActions.put(TEXT_ALIGNMENT_CENTER_ACTION, new TextAlignmentAction(TEXT_ALIGNMENT_CENTER_ACTION, StyleConstants.ALIGN_CENTER));
		defaultActions.put(TEXT_ALIGNMENT_RIGHT_ACTION, new TextAlignmentAction(TEXT_ALIGNMENT_RIGHT_ACTION, StyleConstants.ALIGN_RIGHT));
		defaultActions.put(TEXT_ALIGNMENT_JUSTIFY_ACTION, new TextAlignmentAction(TEXT_ALIGNMENT_JUSTIFY_ACTION, StyleConstants.ALIGN_JUSTIFIED));

		// editor pane
		defaultActions.put(PANE_ZOOM_IN, new PaneZoomAction(PANE_ZOOM_IN, 1.1));
		defaultActions.put(PANE_ZOOM_OUT, new PaneZoomAction(PANE_ZOOM_OUT, 1 / 1.1));
	}

	public static JVGAction getAction(String actionName) {
		return defaultActions.get(actionName);
	}

	private JVGFactory factory = null;

	public void setFactory(JVGFactory factory) {
		this.factory = factory;
	}

	public JVGFactory getFactory() {
		if (factory == null) {
			factory = JVGFactory.createDefault();
		}
		return factory;
	}
}
