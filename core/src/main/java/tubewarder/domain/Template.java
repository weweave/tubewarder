package tubewarder.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import java.util.List;

@Entity
public class Template extends AbstractPersistentObject {
    @Column(unique = true)
    private String name;
    @OneToMany(mappedBy = "template", fetch = FetchType.EAGER)
    private List<ChannelTemplate> channelTemplates;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ChannelTemplate> getChannelTemplates() {
        return channelTemplates;
    }

    public void setChannelTemplates(List<ChannelTemplate> channelTemplates) {
        this.channelTemplates = channelTemplates;
    }
}
