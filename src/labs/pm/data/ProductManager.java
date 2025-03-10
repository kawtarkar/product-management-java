/*
 * Copyright (c) 2025. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package labs.pm.data;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.FormatStyle;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author kawtar
 **/
public class ProductManager {
    private Product product;
    private Review review;
//    private ResourceFormatter formatter;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final Lock writeLock = lock.writeLock();
    private final Lock readLock =lock.readLock();
    private static final Map<String, ResourceFormatter> formatters =
            Map.of ("en-GB", new ResourceFormatter(Locale.UK) ,
                    "en-US", new ResourceFormatter(Locale.US) ,
                    "ru-RU", new ResourceFormatter (new Locale ("ru", "RU")),
                    "fr-FR", new ResourceFormatter (Locale.FRANCE) ,
                    "zh-CN", new ResourceFormatter (Locale.CHINA) ) ;

    private static final Logger logger = Logger.getLogger(ProductManager.class.getName());
    private Map<Product, List<Review>> products= new HashMap<>();
    private final ResourceBundle config = ResourceBundle.getBundle("labs.pm.data.config");
    private final MessageFormat productFormat = new MessageFormat(config.getString("product.data.format"));
    private final MessageFormat reviewFormat = new MessageFormat(config.getString("review.data.format"));

    private final Path reportsFolder = Path.of(config.getString("reports.folder"));
    private final Path dataFolder = Path.of(config.getString("data.folder"));
    private final Path tempFolder = Path.of(config.getString("temp.folder"));
    private  static final  ProductManager pm = new ProductManager();

    private ProductManager () {
//        changeLocale (languageTag) ;
        loadAllData();
    }
    public static ProductManager getInstance(){
        return pm;
    }
//    public ProductManager (Locale locale) {
//        this(locale.getLanguage());
//    }
    public ResourceFormatter changeLocale (String languageTag) {
        return formatters.getOrDefault (languageTag, formatters.
                get ("en-GB") ) ;
    }
    public static Set<String> getSupportedLocales () {
        return formatters.keySet () ;
    }

    public Product creatProduct(int id , String name,
                                BigDecimal price, Rating rating, LocalDate bestBefore) {
       Product product=null ;
       try{
           writeLock.lock();
           product=  new Food (id,name,price,rating,bestBefore);
           products.putIfAbsent(product,new ArrayList<>());

       } catch (Exception e) {
           logger.log(Level.INFO , "Error adding product "+ e.getMessage());
           return null;
       }finally {
           writeLock.unlock();
       }
        return product;
    }
    public Product creatProduct(int id , String name,
                                BigDecimal price, Rating rating) {
        Product product=null ;
        try{
            writeLock.lock();
            product= new Drink (id,name,price,rating);
            products.putIfAbsent(product,new ArrayList<>());

        } catch (Exception e) {
            logger.log(Level.INFO , "Error adding product "+ e.getMessage());
            return null;
        }finally {
            writeLock.unlock();
        }
        return  product;
    }
    public Product findProduct(int id) throws ProductManagerException{
        try {
            readLock.lock();
            return products.keySet()
                    .stream()
                    .filter(p -> p.getId() == id)
                    .findFirst().orElseThrow(
                            ()->new ProductManagerException("Product with id "+ id +" not found"));
        }finally {
            readLock.unlock();
        }
    }

    private Product reviewProduct (Product product, Rating rating, String comments){
        List<Review> reviews=products.get(product);
        products.remove(product);
        reviews.add(new Review(rating, comments));
        product=product.applyRating(
                Rateable.convert(
                (int)Math.round(
                reviews.stream().mapToInt(r->r.rating().ordinal()).average().orElse(0))));
        products.put(product,reviews);
        return product;

    }
    public Product reviewProduct (int productId, Rating rating, String comments){
        try {
            writeLock.lock();
            return reviewProduct(findProduct(productId),rating,comments);
        } catch (ProductManagerException e) {
            logger.log(Level.INFO, e.getMessage());
            return null;
        }finally {
            writeLock.unlock();
        }

    }

    public void printProductReport (int productId ,String languageTag ,String client) {
        try {
            readLock.lock();
            printProductReport(findProduct(productId), languageTag, client);
        } catch (ProductManagerException e) {
            logger.log(Level.INFO,e.getMessage());

        } catch (IOException e) {
            logger.log(Level.SEVERE,"Error printing product report "+e.getMessage(),e);
        }finally {
            readLock.unlock();
        }
    }
    private void printProductReport (Product product, String languageTag, String client) throws IOException {
        ResourceFormatter formatter= changeLocale(languageTag);
        List<Review> reviews = products.get(product);
        Collections.sort(reviews);
        Path productFile = reportsFolder.resolve(MessageFormat
                .format(config.getString("report.file"),product.getId()),client);
        try(PrintWriter out = new PrintWriter(
                new OutputStreamWriter(
                        Files.newOutputStream(productFile, StandardOpenOption.CREATE),"UTF-8"))) {
            out.append(formatter.formatProduct(product)+System.lineSeparator());
            if(reviews.isEmpty()){
                out.append(formatter.getText("no.reviews")+System.lineSeparator());
            }
            else{
                out.append(reviews.stream()
                    .map(r->formatter.formatReview(r)+System.lineSeparator())
                    .collect(Collectors.joining()));
            }
        }

    }
    public void printProducts(Comparator<Product> sorted, Predicate<Product> filter ,String languageTag){
        try {
            readLock.lock();
            ResourceFormatter formatter= changeLocale(languageTag);

            StringBuilder txt = new StringBuilder();

            products.keySet()
                    .stream()
                    .sorted(sorted)
                    .filter(filter)
                    .forEach(p->txt.append(formatter.formatProduct(p)+'\n'));

            System.out.println(txt);
        }finally {
            readLock.unlock();
        }
    }
    private Review parseReview(String text) {
        Review review = null;
        try {
            Object [] values =reviewFormat.parse(text);
            review = new Review(Rateable.convert(Integer.parseInt((String)values[0])), (String)values[1]);

//            reviewProduct(Integer.parseInt((String)values[0]),
//                    Rateable.convert(Integer.parseInt((String)values[1])), (String)values[2]);
        } catch (ParseException | NumberFormatException e) {
            logger.log(Level.WARNING,"Error parsing review"+text,e);

        }
        return  review;
    }
    private Product parseProduct(String text){
        Product product = null;
        try {
            Object [] values =productFormat.parse(text);
            int id = Integer.parseInt((String)values[1]);
            String name = (String)values[2];
            BigDecimal price = BigDecimal.valueOf(Double.parseDouble((String)values[3]));
            Rating rating = Rateable.convert(Integer.parseInt((String)values[4]));
            switch ((String)values[0]){
                case "D":
                    product= new Drink(id,name,price,rating);
//                    creatProduct(id,name,price,rating);
                    break;
                case "F":
                    LocalDate bestBefore = LocalDate.parse((String)values[5]);
                    product= new Food(id,name,price,rating,bestBefore);
//                    creatProduct(id,name,price,rating,bestBefore);
                    break;
            }

        } catch (ParseException | NumberFormatException | DateTimeParseException e) {
            logger.log(Level.WARNING,"Error parsing product "
                    +"text"+ e.getMessage());
        }
        return product;
    }

    private List<Review> loadReviews (Product product){
        List<Review> reviews = null;
        Path file = dataFolder.resolve(
                MessageFormat.format(
                        config.getString("reviews.data.file"),product.getId()));
        if (Files.notExists(file)) {
            reviews = new ArrayList<> ();
        }else{
            try {
                reviews = Files.lines (file, Charset.forName ("UTF-8") )
                        .map (text -> parseReview (text) )
                        . filter (review -> review != null)
                        . collect (Collectors.toList ()) ;
            } catch (IOException e) {
                logger.log(Level.WARNING,"Error loading reviews " + e.getMessage());
            }

        }
        return reviews;

    }
    private Product loadProduct (Path file){
        Product product = null ;
        try {
            product = parseProduct(Files.lines(dataFolder.resolve(file),
                    Charset.forName ("UTF-8")).findFirst().orElseThrow());
        } catch (IOException e) {
            logger.log(Level.WARNING,"Error loading product " + e.getMessage());
        }
        return product;
    }

    private void loadAllData(){
        try {
            products= Files.list(dataFolder)
                    .filter(f->f.getFileName().toString().startsWith("product"))
                    .map(file->loadProduct(file))
                    .filter(product -> product !=null)
                    .collect(Collectors.toMap(product->product,product->loadReviews(product)));
        } catch (IOException e) {
            logger.log(Level.WARNING,"Error loading data " + e.getMessage());
        }
    }
    private void dumpData(){
        try{
            if(Files.notExists(tempFolder)){
                Files.createDirectory(tempFolder);
            }
            Path tempFile = tempFolder.resolve(
                    MessageFormat.format(config.getString("temp.file"), "hh"));
            try (ObjectOutputStream out = new ObjectOutputStream(
                    Files.newOutputStream(tempFile,StandardOpenOption.CREATE))){
                out.writeObject(products);
                products = new HashMap<>();
            }

        }catch(IOException e ){
            logger.log(Level.SEVERE,"Error dumping data " + e.getMessage());
        }
    }
    @SuppressWarnings("unchecked")
    private void restoreData(){
        try{
            Path tempFile = Files.list(tempFolder)
                    .filter(path->path.getFileName().toString().endsWith("tmp"))
                    .findFirst().orElseThrow();
            try (ObjectInputStream in = new ObjectInputStream(
                    Files.newInputStream(tempFile,StandardOpenOption.DELETE_ON_CLOSE))){
                products = (HashMap)in.readObject();

            }

        }catch(IOException | ClassNotFoundException e ){
            logger.log(Level.SEVERE,"Error restoring data " + e.getMessage());
        }
    }

//    public Map <String,String> getDiscount(String languageTag){
//    try{
//        readLock.lock();
//    ResourceFormatter formatter= changeLocale(languageTag);
//        return products.keySet()
//                .stream()
//                .collect(
//                        Collectors.groupingBy(
//                                product->product.getRating().getStars()),
//                        Collectors.summarizingDouble(product->product.getDiscount().doubleValue),
//                        discount->formatter.moneyFormat.format(discount));
//    }finally{
//        readLock.unlock();
//    }

//    }

    private static class ResourceFormatter{
        private Locale locale;
        private ResourceBundle resources;
        private DateTimeFormatter dateFormat;
        private NumberFormat moneyFormat;

        private ResourceFormatter(Locale local){
            this.locale=local;
            resources = ResourceBundle.getBundle("labs.pm.data.resources", locale);
            dateFormat = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).localizedBy(locale);
            moneyFormat = NumberFormat.getCurrencyInstance(locale);
        }
        private String formatProduct(Product product){
            String type = (product instanceof Food) ?
                    resources.getString ("food") : resources.getString ("drink");
            return MessageFormat.format(resources.getString("product"), product.getName(),
                    moneyFormat.format(product.getPrice()),
                    product.getRating().getStars(), type
//                dateFormat.format(product.
                    );
        }
        private String formatReview(Review review){
            return MessageFormat.format (resources.getString ("review"),
                    review.rating().getStars(),
                    review.comments() );
        }
        private String getText (String key) {
            return resources.getString (key) ;
        }

    }


}
