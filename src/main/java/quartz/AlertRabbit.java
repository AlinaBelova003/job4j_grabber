package quartz;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

/**
 * Quartz — очень сложный фреймворк. Он хранит выполненные работы в БД и для того, чтобы его настроить, необходимо создать множество бинов, один из которых главный – Quartz Sheduler.
 * После создания интерфейс IScheduler можно использовать для добавления, удаления и перечисления заданий и триггеров, а также для выполнения других операций, связанных с планированием (например, приостановка триггера).
 * Объект sсheduler - через него идет подключение. В этот объект добавляются все задачи, которые выполняются периодически
 * JobDetail — используется для определения экземпляров Jobs.
 * JobDataMap используется для хранения любого количества объектов данных, которые мы хотим сделать доступными для экземпляра задания при его выполнении.
 * Теперь вы можете спросить: «Как я могу предоставить свойства/конфигурацию для экземпляра Job?» и «Как я могу отслеживать состояние задания между выполнением?» Ответы на эти вопросы одинаковы: ключом является JobDataMap
 * Объект JobDetail создается клиентом Quartz во время добавления задания в планировщик. Позволяет описать детали выполнения()
 * При срабатывании триггера метод execute() вызывается и выводит его реализацию
 * Триггер – компонент, определяющий расписание, по которому будет выполняться данное Задание
 * Загрузка задачи и триггера в планировщик (scheduler.scheduleJob)
 * После выполнение работы в списке будут две даты
 */
public class AlertRabbit {
    public static void main(String[] args) {
        try {
            List<Long> store = new ArrayList<>();
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.start();
            JobDataMap data = new JobDataMap();
            data.put("store", store);
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
            Thread.sleep(5000);
            scheduler.shutdown();
            System.out.println(store);
        } catch (SchedulerException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static class Rabbit implements Job {

        public Rabbit() {
            System.out.println(hashCode());
        }

        /**
         * Чтобы получить объекты из context используется следующий вызов.
         */
        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
            System.out.println("Rabbit runs here ... ");
            List<Long> store = (List<Long>) context.getJobDetail().getJobDataMap().get("store");
            store.add(System.currentTimeMillis());
        }
    }
}
