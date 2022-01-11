package eionet.xmlconv.jobExecutor;

import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration(value = "environmentbean")

public class EnvironmentBean implements EnvironmentAware {

    private Environment environment;

    @Override
    public void setEnvironment(final Environment environment) {
        this.environment = environment;
    }

}
