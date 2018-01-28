package games.strategy.engine.pbem;

import games.strategy.engine.framework.startup.ui.editors.EditorPanel;
import games.strategy.engine.framework.startup.ui.editors.EmailSenderEditor;
import games.strategy.triplea.help.HelpSupport;

/**
 * A pre configured Email sender that uses Hotmail's SMTP server.
 */
public class HotmailEmailSender extends GenericEmailSender {
  private static final long serialVersionUID = 3511375113962472063L;

  /**
   * Initializes a new instance of the {@code HotmailEmailSender} class.
   */
  public HotmailEmailSender() {
    setHost("smtp.live.com");
    setPort(587);
    setEncryption(Encryption.TLS);
  }

  @Override
  public EditorPanel getEditor() {
    return new EmailSenderEditor(this, new EmailSenderEditor.EditorConfiguration());
  }

  @Override
  public String getDisplayName() {
    return "Hotmail ";
  }

  @Override
  public IEmailSender clone() {
    final GenericEmailSender sender = new HotmailEmailSender();
    sender.setSubjectPrefix(getSubjectPrefix());
    sender.setPassword(getPassword());
    sender.setToAddress(getToAddress());
    sender.setUserName(getUserName());
    sender.setAlsoPostAfterCombatMove(getAlsoPostAfterCombatMove());
    sender.setCredentialsSaved(areCredentialsSaved());
    return sender;
  }

  @Override
  public String getHelpText() {
    return HelpSupport.loadHelp("hotmailEmailSender.html");
  }
}
