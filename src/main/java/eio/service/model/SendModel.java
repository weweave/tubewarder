package eio.service.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;
import java.util.Map;

@XmlRootElement
public class SendModel {
    @XmlElement(required = true)
    public String template;
    @XmlElement(required = true)
    public String channel;
    @XmlElement(required = true)
    public AddressModel recipient;
    public Map<String, Object> model;
    public List<AttachmentModel> attachments;
}
