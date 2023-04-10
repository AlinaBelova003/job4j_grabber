package quartz;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ScheduledFuture;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

/**
 * Quartz — фреймворк. Чтобы его настроить, необходимо создать множество бинов, один из которых главный – Sheduler.
 * Его можно использовать для добавления, удаления и перечисления заданий и триггеров, других операций, связанных с планированием(приостановка триггера).
 * JobDetail — используется для уточнения деталей работы
 * JobDataMap используется для хранения любого количества объектов данных(свойств\ конфигураций), которые мы хотим сделать доступными для экземпляра задания при его выполнении.
 * Триггер – компонент, определяющий расписание, по которому будет выполняться данное Задание
 * При срабатывании триггера метод execute() вызывается и выводит его реализацию
 * Загрузка задачи и триггера планировщику (scheduler.scheduleJob)
 * scheduler.shutdown() - завершение\ закрытие планировщика
 */
public class AlertRabbit implements AutoCloseable {
    public static void main(String[] args) {
        try (Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler()) {
            scheduler.start();
            JobDataMap data = new JobDataMap();
            data.put("store", "?");
            JobDetail job = newJob(Rabbit.class)
                    .usingJobData(data)
                    .build();
            SimpleScheduleBuilder times = SimpleScheduleBuilder.simpleSchedule()
                    .withIntervalInSeconds(5)
                    .repeatForever();
            Trigger trigger = newTrigger()
                    .startNow()
                    .withSchedule(times)
                    .build();
            scheduler.scheduleJob(job, trigger);
            Thread.sleep(10000);
            scheduler.shutdown();
        } catch (SchedulerException | InterruptedException e) {
            e.printStackTrace();
        }
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

    @Override
    public void close() throws Exception {
        throw new NullPointerException("Thrown from close()");
    }

    public static class Rabbit implements Job {

        public Rabbit() {
            System.out.println(hashCode());
        }

        /**
         * Этот метод будет срабатывать каждый раз, когда будет срабатывать триггер
         * Здесь помещен основной код
         * Чтобы получить объекты из context используется следующий вызов.
         */
        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
            System.out.println("Запись выполнена в : ");
            List<Long> store = (List<Long>) context.getJobDetail().getJobDataMap().get("store");
            store.add(System.currentTimeMillis());
        }
    }
}
