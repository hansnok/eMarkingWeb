/**
 * 
 */
package cl.uai.client.toolbar.buttons;

import java.util.Map;
import java.util.logging.Logger;

import cl.uai.client.EMarkingConfiguration;
import cl.uai.client.EMarkingWeb;
import cl.uai.client.MarkingInterface;
import cl.uai.client.data.AjaxData;
import cl.uai.client.data.AjaxRequest;
import cl.uai.client.marks.Mark;
import cl.uai.client.marks.RubricMark;
import cl.uai.client.page.MarkingPage;
import cl.uai.client.resources.Resources;
import cl.uai.client.toolbar.FinishMarkingDialog;

import com.github.gwtbootstrap.client.ui.Icon;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * @author Jorge Villalón
 *
 */
public class ExamButtons extends Buttons {

	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(ExamButtons.class.getName());
	
	private PushButton finishMarkingButton = null;
	public PushButton getFinishMarkingButton() {
		return finishMarkingButton;
	}

	public PushButton getSaveChangesButton() {
		return saveChangesButton;
	}

	public PushButton getSaveAndJumpToNextButton() {
		return saveAndJumpToNextButton;
	}

	private PushButton saveChangesButton = null;
	private PushButton saveAndJumpToNextButton = null;
	private ToggleButton selectAsAnswerKey = null;
	private boolean includeTextInButtons = true;

	
	public ExamButtons(boolean includeText) {
		this.includeTextInButtons = includeText;
		
		finishMarkingButton = new PushButton(IconType.CHECK, includeTextInButtons ? MarkingInterface.messages.FinishMarking() : null);
		finishMarkingButton.setVisible(false);
		finishMarkingButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				// Count the total marks, rubric mark more toolbar mark
								int allRubricMark = 0;
								// If a rubric mark  is eliminated this becomes invisible
								// Count how many rubric marks have been removed
								int invisibleMark = 0;
								VerticalPanel pages = EMarkingWeb.markingInterface.getMarkingPagesInterface().getPagesPanel();
								for(int i = 0; i < pages.getWidgetCount() ; i++){
									Map<Integer, Mark> marks = ((MarkingPage) pages.getWidget(i)).getMarkWidgets();
									allRubricMark+= ((MarkingPage)pages.getWidget(i)).isHaveRubricMark();
				 					for(Mark m : marks.values()) {
				 						if(m instanceof RubricMark){
				 							if(!m.isVisible()){
												invisibleMark++;
				 							}
				 						}
				 					}
				 				}
			if( (allRubricMark > 0 && invisibleMark == 0) || (allRubricMark > invisibleMark && allRubricMark > 0)){				
				if(MarkingInterface.submissionData != null && MarkingInterface.submissionData.getId() > 0) {
					
					FinishMarkingDialog finishDialog = new FinishMarkingDialog();
					finishDialog.setGeneralFeedback(EMarkingWeb.markingInterface.getRubricInterface().getRubricPanel().getGeneralFeedbackInterface().getFeedbackText());
					
					finishDialog.addCloseHandler(new CloseHandler<PopupPanel>() {						
						@Override
						public void onClose(CloseEvent<PopupPanel> event) {
							FinishMarkingDialog dialog = (FinishMarkingDialog) event.getSource();
							
							if(dialog.isCancelled())
								return;
							
							EMarkingWeb.markingInterface.addLoading(true);
							
							AjaxRequest.ajaxRequest("action=finishmarking"
									+ "&feedback=" + URL.encode(dialog.getGeneralFeedback()), 
									new AsyncCallback<AjaxData>() {
								
								@Override
								public void onSuccess(AjaxData result) {
									Map<String, String> values = AjaxRequest.getValueFromResult(result);
									
									if(!values.get("error").equals("")) {
										Window.alert(MarkingInterface.messages.ErrorFinishingEmarking());
										EMarkingWeb.markingInterface.finishLoading();
									} else {
										if(!EMarkingWeb.closeAndReload()) {
											EMarkingWeb.markingInterface.finishLoading();
											Window.alert(MarkingInterface.messages.FinishingMarkingSuccessfull());
										}
									}
								}
								
								@Override
								public void onFailure(Throwable caught) {
									Window.alert(MarkingInterface.messages.ErrorFinishingEmarking());
									EMarkingWeb.markingInterface.finishLoading();
								}
							});
						}
					});
					finishDialog.center();
				}
			}else{
				//It has not been corrected any questions
				Window.alert("You need to add one or more criteria in the rubric");
			}
			}
		});
		
		saveChangesButton = new PushButton(IconType.SAVE, includeTextInButtons ? MarkingInterface.messages.SaveChangesClose() : null);
		saveChangesButton.setVisible(false);
		saveChangesButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
					EMarkingWeb.closeAndReload();

			}
		});
		
		selectAsAnswerKey = new ToggleButton();
		selectAsAnswerKey.addStyleName(Resources.INSTANCE.css().rubricbutton());
		Icon i = new Icon(IconType.TROPHY);
		selectAsAnswerKey.setHTML(i.toString() + (includeTextInButtons ? ("<div class=\""+Resources.INSTANCE.css().rubricbuttontext()+"\">" +
				MarkingInterface.messages.AnswerKey() + "</div>") : ""));
		selectAsAnswerKey.setVisible(true);
		selectAsAnswerKey.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				EMarkingWeb.markingInterface.addLoading(true);
				
				AjaxRequest.ajaxRequest("action=setanswerkey&status=1", new AsyncCallback<AjaxData>() {					
					@Override
					public void onSuccess(AjaxData result) {
						EMarkingWeb.markingInterface.finishLoading();
						if(!result.getError().equals("")) {
							Window.alert("Error on ajax request for setting as answwer key");
							return;
						}
						Map<String, String> values = AjaxRequest.getValueFromResult(result);
						String newvalue = values.get("newvalue");
						if(newvalue != null && newvalue.equals("1")) {
							selectAsAnswerKey.setDown(true);
						} else {
							selectAsAnswerKey.setDown(false);
						}
					}					
					@Override
					public void onFailure(Throwable caught) {
						Window.alert("Error trying to select as answer key");
					}
				});
			}
		});
		
		saveAndJumpToNextButton = new PushButton(IconType.EXTERNAL_LINK, includeTextInButtons ?  MarkingInterface.messages.JumpToNextStudent() : null);
		saveAndJumpToNextButton.setVisible(false);
		saveAndJumpToNextButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				EMarkingWeb.markingInterface.addLoading(true);

				AjaxRequest.ajaxRequest("action=getnextsubmission", 
						new AsyncCallback<AjaxData>() {

					@Override
					public void onFailure(Throwable caught) {
						EMarkingWeb.markingInterface.finishLoading();
						Window.alert("Epic fail");
					}

					@Override
					public void onSuccess(AjaxData result) {
						Map<String, String> values = AjaxRequest.getValueFromResult(result);
						int nextsubmission = Integer.parseInt(values.get("nextsubmission"));
						if(nextsubmission > 0) {
							EMarkingWeb.markingInterface.finishLoading();
							MarkingInterface.setDraftId(nextsubmission);
							EMarkingWeb.markingInterface.loadSubmissionData();
							loadSubmissionData();
							return;
						} else {
							Window.alert(MarkingInterface.messages.NoMoreSubmissions());
							EMarkingWeb.markingInterface.finishLoading();							
						}
					}
				});
			}
		});

		if(this.includeTextInButtons) {
			this.mainPanel.add(saveAndJumpToNextButton);
		}
		this.mainPanel.add(saveChangesButton);
		this.mainPanel.add(finishMarkingButton);
		if(this.includeTextInButtons) {
			this.mainPanel.add(selectAsAnswerKey);
			saveChangesButton.removeStyleName(Resources.INSTANCE.css().rubricbutton());
			finishMarkingButton.removeStyleName(Resources.INSTANCE.css().rubricbutton());
		}
	}
	
	public void loadSubmissionData() {
		saveChangesButton.setVisible(true);
		finishMarkingButton.setVisible(false);
		saveAndJumpToNextButton.setVisible(true);
		
		if(EMarkingConfiguration.isSupervisor() && !MarkingInterface.submissionData.isQualitycontrol()
				&& EMarkingConfiguration.getMarkingType() != EMarkingConfiguration.EMARKING_TYPE_PRINT_SCAN) {
			finishMarkingButton.setVisible(true);
		}
		
		boolean isAnswerKey = MarkingInterface.submissionData.isAnswerkey();
		selectAsAnswerKey.setDown(isAnswerKey);
	}
}
