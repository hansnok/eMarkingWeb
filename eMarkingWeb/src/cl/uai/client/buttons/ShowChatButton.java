package cl.uai.client.buttons;

import cl.uai.client.EMarkingWeb;

import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.google.gwt.event.dom.client.ClickEvent;

public class ShowChatButton extends BubbleButton {

	public ShowChatButton(int _left, int _top, int _source) {
		super(IconType.COMMENTS, _left, _top, _source);
	}
	
	@Override
	public void onButtonClick(ClickEvent event) {
		EMarkingWeb.markingInterface.chat.center();
		EMarkingWeb.markingInterface.chat.show();
	}
}
