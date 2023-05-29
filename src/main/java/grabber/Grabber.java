package grabber;

import grabber.utils.HabrCareerDateTimeParser;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

public class Grabber implements Grab {
    private final Parse parse;
    private final Store store;
    private final Scheduler scheduler;
    private final int time;

    private final Properties properties = new Properties();

    public Grabber(Parse parse, Store store, Scheduler scheduler, int time) {
        this.parse = parse;
        this.store = store;
        this.scheduler = scheduler;
        this.time = time;
    }

    public Grabber() {
    }

    public static class GrabJob implements Job {
        /**
         * Этот метод извлекает и сохраняет список записей с указанной страницы, используя объекты parse и store.
         */
        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
            JobDataMap map = context.getJobDetail().getJobDataMap();
            Store store = (Store) map.get("store");
            Parse parse = (Parse) map.get("parse");
            try {
                for (Post post : parse.list("https://career.habr.com/vacancies/java_developer?page=1")) {
                    store.save(post);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Этот код представляет метод web(), который запускает веб-сервер на указанном порту и выводит все записи из объекта store в ответ на любой запрос.
     *Внутри метода создается новый поток, который слушает входящие соединения на указанном порту. Когда соединение установлено, метод отправляет HTTP-ответ со статусом 200 OK и выводит все записи из объекта store в ответ на запрос.
     *В цикле for происходит итерация по всем записям в объекте store с помощью метода getAll(). Для каждой записи метод toString() вызывается для преобразования записи в строку, которая затем отправляется в ответ на запрос.
     * @param store
     */
    public void web(Store store) {
        new Thread(() -> {
            try (ServerSocket server = new ServerSocket(Integer.parseInt(properties.getProperty("port")))) {
                while (!server.isClosed()) {
                    Socket socket = server.accept();
                    try (OutputStream out = socket.getOutputStream()) {
                        out.write("HTTP/1.1 200 OK\r\n\r\n".getBytes());
                        for (Post post : store.getAll()) {
                            out.write(post.toString().getBytes());
                            out.write(System.lineSeparator().getBytes());
                        }
                    } catch (IOException io) {
                        io.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Этот код читает свойство properties
     */
    public void cfg() {
        Properties config = new Properties();
        try (InputStream in = Grabber.class.getClassLoader().getResourceAsStream("post.properties")) {
            config.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Этот код создаёт экземпляр планировщика, и запускает его
     */
    public Scheduler scheduler() throws SchedulerException {
        Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
        scheduler.start();
        return scheduler;
    }

    /**
     * Этот код берет данные из properties, подключается к бд и загружает данные
     */
    private Store store() {
        return new PsqlStore(properties);
    }

    @Override
    public void init(HabrCareerParse parse, Store store, Scheduler scheduler) throws SchedulerException {
        JobDataMap data = new JobDataMap();
        data.put("store", store);
        data.put("parse", parse);
        JobDetail job = newJob(GrabJob.class)
                .usingJobData(data)
                .build();
        SimpleScheduleBuilder times = simpleSchedule()
                .withIntervalInSeconds(time)
                .repeatForever();
        Trigger trigger = newTrigger()
                .startNow()
                .withSchedule(times)
                .build();
        scheduler.scheduleJob(job, trigger);
        }


    public static void main(String[] args) throws Exception {
        Grabber grab = new Grabber();
        grab.cfg();
        Scheduler scheduler = grab.scheduler();
        Store store = grab.store();
        grab.init(new HabrCareerParse(new HabrCareerDateTimeParser()), store, scheduler);
        grab.web(store);
    }
}
