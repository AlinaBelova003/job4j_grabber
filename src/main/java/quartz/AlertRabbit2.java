package quartz;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

/**
 * Чтение из файла rabbit.properties с периодичностью в 10 секунд
 * withIntervalInSeconds() ожидает на вход число, а properties - это строка
 */
public class AlertRabbit2 {
    public static void main(String[] args) throws SchedulerException {
        Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
        scheduler.start();
        JobDetail job = newJob(AlertRabbit2.Rabbit2.class).build();
        SimpleScheduleBuilder times = SimpleScheduleBuilder.simpleSchedule()
                .withIntervalInSeconds(Integer.parseInt(readeProperties().getProperty("rabbit.properties")))
                .repeatForever();
        Trigger trigger = newTrigger()
                .startNow()
                .withSchedule(times)
                .build();
        scheduler.scheduleJob(job, trigger);
    }

    public static Properties readeProperties() {
        Properties config = new Properties();
        try (InputStream in = AlertRabbit2.class.getClassLoader().getResourceAsStream("rabbit.properties")) {
            config.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return config;
    }

    public static class Rabbit2 implements Job {

        @Override
        public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
            System.out.println("Rabbit runs here ... ");

        }
    }
}
