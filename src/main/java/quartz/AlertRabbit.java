package quartz;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.util.concurrent.ScheduledFuture;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

/**
 * Объект sсheduler - через него идет подключение. В этот объект добавляются все задачи, которые выполняются периодически
 * JobDetail — используется для определения экземпляров Jobs.
 * Объект JobDetail создается клиентом Quartz во время добавления задания в планировщик. По сути, это определение экземпляра задания
 * При срабатывании триггера метод execute() вызывается и выводит его реализацию
 * Триггер – компонент, определяющий расписание, по которому будет выполняться данное Задание
 * Загрузка задачи и триггера в планировщик (scheduler.scheduleJob)
 */
public class AlertRabbit {
    public static void main(String[] args) {
        try {
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.start();
            JobDetail job = newJob(Rabbit.class).build();
            SimpleScheduleBuilder times = SimpleScheduleBuilder.simpleSchedule()
                    .withIntervalInSeconds(10)
                    .repeatForever();
            Trigger trigger = newTrigger()
                    .startNow()
                    .withSchedule(times)
                    .build();
            scheduler.scheduleJob(job, trigger);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    public static class Rabbit implements Job {

        @Override
        public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
            System.out.println("Rabbit runs here ... ");
        }
    }
}
