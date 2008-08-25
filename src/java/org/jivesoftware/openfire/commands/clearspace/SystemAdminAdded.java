package org.jivesoftware.openfire.commands.clearspace;

import org.jivesoftware.openfire.commands.AdHocCommand;
import org.jivesoftware.openfire.commands.SessionData;
import org.jivesoftware.openfire.admin.AdminManager;
import org.jivesoftware.openfire.component.InternalComponentManager;
import org.dom4j.Element;
import org.xmpp.forms.DataForm;
import org.xmpp.forms.FormField;
import org.xmpp.packet.JID;

import java.util.List;
import java.util.Map;
import java.util.Arrays;

/**
 * Notifies that a new system administrator has been added.
 *
 * @author Armando Jagucki
 */
public class SystemAdminAdded extends AdHocCommand {
    public String getCode() {
        return "http://jabber.org/protocol/event#sys-admin-added";
    }

    public String getDefaultLabel() {
        return "System administrator added";
    }

    public int getMaxStages(SessionData data) {
        return 1;
    }

    public void execute(SessionData sessionData, Element command) {
        Element note = command.addElement("note");

        Map<String, List<String>> data = sessionData.getData();

        // Get the username of the new admin
        String adminUsername;
        try {
            adminUsername = get(data, "adminUsername", 0);
        }
        catch (NullPointerException npe) {
            note.addAttribute("type", "error");
            note.setText("Admin username required parameter.");
            return;
        }

        // Promotes the user to administrator, locally
        AdminManager.getInstance().addAdminAccount(adminUsername);

        // Answer that the operation was successful
        note.addAttribute("type", "info");
        note.setText("Operation finished successfully");
    }

    protected void addStageInformation(SessionData data, Element command) {
        DataForm form = new DataForm(DataForm.Type.form);
        form.setTitle("Dispatching a system admin added event.");
        form.addInstruction("Fill out this form to dispatch a system admin added event.");

        FormField field = form.addField();
        field.setType(FormField.Type.hidden);
        field.setVariable("FORM_TYPE");
        field.addValue("http://jabber.org/protocol/admin");

        field = form.addField();
        field.setType(FormField.Type.text_single);
        field.setLabel("The username of the new system administrator.");
        field.setVariable("adminUsername");
        field.setRequired(true);

        // Add the form to the command
        command.add(form.getElement());
    }

    protected List<Action> getActions(SessionData data) {
        return Arrays.asList(Action.complete);
    }

    protected Action getExecuteAction(SessionData data) {
        return Action.complete;
    }

    @Override
    public boolean hasPermission(JID requester) {
        return super.hasPermission(requester) || InternalComponentManager.getInstance().hasComponent(requester);
    }
}