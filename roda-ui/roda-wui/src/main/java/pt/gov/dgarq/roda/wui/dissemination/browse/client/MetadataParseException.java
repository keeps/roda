package pt.gov.dgarq.roda.wui.dissemination.browse.client;

import java.util.ArrayList;
import java.util.List;

public class MetadataParseException extends Exception {
  private static final long serialVersionUID = -4784198919577336934L;

  private List<ParseError> errors;

  public MetadataParseException() {
    super();
    errors = new ArrayList<ParseError>();
  }

  public MetadataParseException(String message) {
    super(message);
    errors = new ArrayList<ParseError>();
  }

  public List<ParseError> getErrors() {
    return errors;
  }

  public void setErrors(List<ParseError> errors) {
    this.errors = errors;
  }

}
