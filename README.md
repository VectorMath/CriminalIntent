# CriminalIntent

[TOC]

## Описание проекта
**CriminalIntent** является pet-проектом, которое было написано в ходе обучения по книге "Android. Программирование для профессионалов 4-е издание."

Приложение преднозначено для хранения информации об офисных преступлениях, пользователь создаёт запись о преступлениях с заголовком, датой и фотографией. Так же можно выбрать подозреваемого в адресной книге и отправить отчёт на электронную почту, соц.сеть или мессенджер.

## Что использовалось при разработке
- Язык программирования **Kotlin**

- **Отображение данных**
  - RecyclerView
  - ViewModel
- **Для создания локальной БД**
  - Room
  - LiveData
- **Паттерны**
  - MVC
  - Singletone

- **Диалоговые окна**

## Паттерны проектирования
При создание приложения использовались паттерны проектирования **MVC** (Model-View-Controller) и **Singletone**.
### MVC
В MVC была реализовано большая часть работы над приложением:
- **Model** - класс Crime.
- **Views** - XML Layouts.
- **Controllers** - Fragments и MainActivity.
#### Model

------------


##### Crime.kt
Единственный класс, который реализует модель преступления. Имеет следующие поля:
- **id** - создаётся в конструкторе при помощи метода randomUUID() класса UUID
- **title** - Заголовок преступления.
- **date** - Дата преступления.
- **isSolved** - булевое значение, которое отвечает за то решено ли преступление или нет.
- **suspect** - Подозреваемый в преступление, значение берется из книги контактов.
- **photoFileName** - Название изображения, которое создаётся при снимке.

Ниже предоставлен код файла **Crime.kt**
```kotlin
@Entity
data class Crime(
    @PrimaryKey val id: UUID = UUID.randomUUID(),
    var title: String = "",
    var date: Date = Date(),
    var isSolved: Boolean = false,
    var suspect: String = ""
) {
    val photoFileName
        get() = "IMG_$id.jpg"
}
```

------------



#### Views
##### activity_main.xml
Является своего рода контейнером для фрагментов. Кроме FrameLayout больше здесь ничего не находится.
##### fragment_crime.xml
Внешний вид определенного преступления
![fragment_crime](/docs/layouts-screen/fragment-crime.jpg)
##### fragment_crime_list.xml
Layout, где храниться список элементов RecyclerView.
##### fragment_dialog.xml
Макет фрагмента увеличенной фотографии конкретного преступления.
##### list_item_crime.xml
Внешний вид одного из элементов преступления в списке RecyclerView.
![list-item-crime](/docs/layouts-screen/list-item-crime.jpg)

------------

#### Controllers


##### CrimeFragment.kt
Отвечает за весь функционал, который представлен в **fragment_crime.xml.**
Основной функционал записан в жизненных циклах фрагмента. Здесь же рассмотрены собственные методы данного класса.

- **updateUI()** - метод, отвечающий за обновление пользовательского интерфейса при произошествии изменение в экземпляре класса Crime.
```kotlin
 private fun updateUI() {
        titleField.setText(crime.title)
        dateButton.text = crime.date.toString()
        solvedCheckBox.apply {
            isChecked = crime.isSolved
            jumpDrawablesToCurrentState()
        }

        if (crime.suspect.isNotEmpty()) {
            suspectButton.text = crime.suspect
        }

        updatePhotoView()
    }
```

- **updatePhotoView()** - метод, отвечающий за обновление фотографии в экземпляре класса Crime.
```kotlin
    private fun updatePhotoView() {
        if (photoFile.exists()) {
            val bitmap = getScaledBitmap(photoFile.path, requireActivity())
            photoView.setImageBitmap(bitmap)
        } else {
            photoView.setImageDrawable(null)
        }
    }
```

- **getCrimeReport(): String** - метод, который возвращает текст отчёта, который передаётся в сообщение мессенджера, соц.сети или письма в эл.почте.
```kotlin
   private fun getCrimeReport(): String {
        val solvedString = if (crime.isSolved) {
            getString(R.string.crime_report_solved)
        } else {
            getString(R.string.crime_report_unsolved)
        }

        val dateString = android.text.format.DateFormat.format(DATE_FORMAT, crime.date).toString()
        val suspect = if (crime.suspect.isBlank()) {
            getString(R.string.crime_report_no_suspect)
        } else {
            getString(R.string.crime_report_suspect, crime.suspect)
        }

        val solvedAndSuspect = "$solvedString, $suspect"

        return getString(
            R.string.crime_report,
            crime.title, dateString, solvedAndSuspect
        )
    }
```
------------

##### CrimeListFragment.kt
Реализует функционал списка RecyclerView и меню добавления нового преступления.

- **CrimeHolder(view: View)** -- внутренний класс, который наследуется от класса **RecyclerView.ViewHolder** и интрефейса **View.OnClickListener** реализует заполнение элемента списка в зависимости от общих данных и данных экземпляра класса **Crime** с помощью метода **bind**, который принимает в себя экземпляр класса, а так же накидывает событие нажатия на элемент, которое позволяет подробнее рассмотреть выбранное преступление, либо же отредактировать его(проще говоря, переход на фрагмент **CrimeFragment**). Ниже представлен полный код данного класса.

```kotlin
    private inner class CrimeHolder(view: View) : RecyclerView.ViewHolder(view),
        View.OnClickListener {

        private lateinit var crime: Crime

        private val titleTextView: TextView = itemView.findViewById(R.id.crime_title)
        private val dateTextView: TextView = itemView.findViewById(R.id.crime_date)
        private val solvedImageView: ImageView = itemView.findViewById(R.id.crime_solved)

        init {
            itemView.setOnClickListener(this)
        }

        fun bind(crime: Crime) {
            this.crime = crime
            titleTextView.text = this.crime.title
            dateTextView.text = this.crime.date.toString()
            solvedImageView.visibility = if (crime.isSolved) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }

        override fun onClick(v: View?) {
            callbacks?.onCrimeSelected(crime.id)
        }
    }
```

- **CrimeAdapter(var crimes: List<Crime>)** -- Внутренний класс, который реализует функционал RecyclerView.Adapter, позволяющий заполнить список RecyclerView имеющимися данными с помощью класса **CrimeHolder**, а так же обновить и добавить в него новые преступления. Имеет следующие методы:
1. **onCreateViewHolder** -- Создаёт внешний вид элемента в списке RecyclerView.
```kotlin
 override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CrimeHolder {
            val view = layoutInflater.inflate(R.layout.list_item_crime, parent, false)
            return CrimeHolder(view)
        }
```

2. **onBindViewHolder** -- Заполняет экземпляр класса Crime по позиции с помощью метода bind класса CrimeHolder.
   
```kotlin
 override fun onBindViewHolder(holder: CrimeHolder, position: Int) {
            val crime = crimes[position]
            holder.bind(crime)
        }
```

3. **getItemCount** -- возвращает кол-во элементов списка.
```kotlin
 override fun getItemCount(): Int {
            return crimes.size
        }
```

4. **updateUI** -- обновляет адаптер, если появились изменения в списке.
```kotlin
private fun updateUI(crimes: List<Crime>) {
        adapter = CrimeAdapter(crimes)
        crimeRecyclerView.adapter = adapter
    }
```
------------


##### DataPickerFragment.kt
Реализует диалоговое окно выбора даты с помощью календаря(что не делает его независимым как другие фрагменты).
Имеет переопределённый метод **onCreateDialog**, который возвращает диалоговое окно даты с установленным в нём значение.

```kotlin
   override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dateListener =
            DatePickerDialog.OnDateSetListener { _: DatePicker, year: Int, month: Int, day: Int ->

                val resultDate: Date = GregorianCalendar(year, month, day).time

                targetFragment?.let { fragment ->
                    (fragment as Callbacks).onDateSelected(resultDate)
                }
            }

        val date = arguments?.getSerializable(ARG_DATE) as Date
        val calendar = Calendar.getInstance()
        calendar.time = date
        val initialYear = calendar.get(Calendar.YEAR)
        val initialMonth = calendar.get(Calendar.MONTH)
        val initialDay = calendar.get(Calendar.DAY_OF_MONTH)

        return DatePickerDialog(
            requireContext(),
            dateListener,
            initialYear,
            initialMonth,
            initialDay
        )
    }
```

------------

##### DialogFragment.kt
Фрагмент, который открывает фотографию конкретного преступления в полноэкранном режиме.

```kotlin
class DialogFragment : Fragment() {
    private lateinit var photoView: ImageView
    private var photoUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        photoUri = arguments?.getParcelable("crime_photo")!!
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_dialog, container, false)

        photoView = view.findViewById(R.id.crime_photo)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        photoView.setImageURI(photoUri)
    }

    companion object {
        fun newInstance(crimeUri: Uri): DialogFragment {
            val args = Bundle().apply {
                putParcelable("crime_photo", crimeUri)
            }
            return DialogFragment().apply {
                arguments = args
            }
        }
    }
}
```

------------

##### MainActivity.kt
Является по своей сути контейнером для текущего фрагмента приложения. Функционал заключается в вызове callback-метода фрагментов. В методе onCreate текущий фрагмент берёт в своё значение экземпляр объекта CrimeListFragment. Ниже предоставлен полный код MainActivity.kt
```kotlin
class MainActivity : AppCompatActivity(), CrimeListFragment.Callbacks, CrimeFragment.Callbacks {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        val fragment: Fragment

        if (currentFragment == null) {

            fragment = CrimeListFragment.newInstance()

            supportFragmentManager
                .beginTransaction()
                .add(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }
    }

    override fun onCrimeSelected(crimeId: UUID) {
        val fragment = CrimeFragment.newInstance(crimeId)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onPhotoScaled(crimeUri: Uri) {
        val fragment = DialogFragment.newInstance(crimeUri)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}
```

------------

### Singletone
В данном проекте был необходим некий "репозиторий" для работы с базой данных в единичном экземпляре, который будет существовать до тех пор пока приложение находится в памяти телефона. Поэтому было решено использовать паттерн Одиночка и на его основе реализовать класс CrimeRepository.

#### CrimeRepository.kt
Инкапсулирует логику доступа к данным из источников. Определяет как брать и хранить набор данных.

```kotlin
class CrimeRepository private constructor(context: Context) {
    private val database: CrimeDataBase = Room.databaseBuilder(
        context.applicationContext,
        CrimeDataBase::class.java,
        DATABASE_NAME
    ).addMigrations(migration_1_2).build()

    private val executor = Executors.newSingleThreadExecutor()
    private val filesDir = context.applicationContext.filesDir
    private val crimeDao = database.crimeDao()

    fun getCrimes(): LiveData<List<Crime>> = crimeDao.getCrimes()

    fun getCrime(id: UUID): LiveData<Crime?> = crimeDao.getCrime(id)

    fun updateCrime(crime: Crime) {
        executor.execute {
            crimeDao.updateCrime(crime)
        }
    }

    fun addCrime(crime: Crime) {
        executor.execute {
            crimeDao.addCrime(crime)
        }
    }

    fun getPhotoFile(crime: Crime): File = File(filesDir, crime.photoFileName)

    companion object {
        private var INSTANCE: CrimeRepository? = null

        fun initialize(context: Context) {
            if (INSTANCE == null) INSTANCE = CrimeRepository(context)
        }

        fun get(): CrimeRepository {
            return INSTANCE ?: throw IllegalStateException("CrimeRepository must be initialized")
        }
    }
}
```

------------

## Реализация базы данных
### Интерфейс CrimeDao
Содержит в себе функции для операций с базой данных, которые мы хотим реализовать.
```kotlin
@Dao
interface CrimeDao {
    @Query("SELECT * FROM crime")
    fun getCrimes(): LiveData<List<Crime>>

    @Query("SELECT * FROM crime WHERE id=(:id)")
    fun getCrime(id: UUID): LiveData<Crime?>

    @Update
    fun updateCrime(crime: Crime)

    @Insert
    fun addCrime(crime: Crime)
}
```
------------
### CrimeDataBase.kt
Класс-сущность, который определяет структуру нашей базы данных. Аннотация **@Database** сообщает Room о том, что класс CrimeDataBase представляет собой БД приложения CriminalIntent.
```kotlin
@Database(entities = [Crime::class], version = 2)
@TypeConverters(CrimeTypeConverters::class)

abstract class CrimeDataBase : RoomDatabase() {
    abstract fun crimeDao(): CrimeDao
}

val migration_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE Crime ADD COLUMN suspect TEXT NOT NULL DEFAULT ''")
    }
}
```
------------
### CrimeTypeConverters.kt
Преобразователь типов который нужен нам для того, чтобы передавать в CrimeDataBase типы данных **Date** и **UUID**
```kotlin
class CrimeTypeConverters {
    @TypeConverter
    fun fromDate(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun toDate(millisSinceEpoch: Long?): Date? {
        return millisSinceEpoch?.let {
            Date(it)
        }
    }

    @TypeConverter
    fun fromUUID(uuid: UUID?): String? {
        return uuid?.toString()
    }

    @TypeConverter
    fun toUUID(uuid: String?): UUID? {
        return UUID.fromString(uuid)
    }
}
```
------------

## Другие файлы
### CriminalIntentApplication.kt
подкласс Application, который позволяет получить информацию о жизненом цикле приложения. Нужен для того, чтобы зарегистрировать его в манифесте приложения, таким образом этот класс будет сработает сразу при запуске приложения, передав в себе экземпляр класса CrimeRepository

```kotlin
class CriminalIntentApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        CrimeRepository.initialize(this)
    }
}
```
### CrimeListViewModel.kt
Реализует ViewModel для сохранения прогресса при изменение жизненных циклов СrimeListFragment.

```kotlin
class CrimeListViewModel : ViewModel() {
    private val crimeRepository = CrimeRepository.get()
    val crimeListLiveData = crimeRepository.getCrimes()

    fun addCrime(crime: Crime) {
        crimeRepository.addCrime(crime)
    }
}
```
### CrimeDetailViewModel.kt
Реализует ViewModel для сохранения прогресса при изменение жизненных циклов СrimeFragment.

```kotlin
class CrimeDetailViewModel() : ViewModel() {
    private val crimeRepository: CrimeRepository = CrimeRepository.get()
    private val crimeIdLiveData = MutableLiveData<UUID>()

    var crimeLiveData: LiveData<Crime?> =
        Transformations.switchMap(crimeIdLiveData) { crimeId ->
            crimeRepository.getCrime(crimeId)
        }

    fun loadCrime(crimeId: UUID) {
        crimeIdLiveData.value = crimeId
    }

    fun saveCrime(crime: Crime) {
        crimeRepository.updateCrime(crime)
    }

    fun getPhotoFile(crime: Crime): File {
        return crimeRepository.getPhotoFile(crime)
    }
}
```
### PictureUtils.kt
Содержит в себе метод getScaledBitmap и его перегруженную версию. Первый метод необходим для того, чтобы уменьшить сделанный снимок до нужным размеров, который в конечном итоге возвращает окончательную версию мини-фотографии в макет fragment-crime.xml

```kotlin
fun getScaledBitmap(path: String, destWidth: Int, destHeight: Int): Bitmap {
    // Чтение размеров изображения на диске
    var options = BitmapFactory.Options()
    options.inJustDecodeBounds = true
    BitmapFactory.decodeFile(path, options)

    val srcWidth = options.outWidth.toFloat()
    val srcHeight = options.outHeight.toFloat()

    // Выясняем, насколько нужно уменьшить
    var inSampleSize = 1
    if (srcHeight > destHeight || srcWidth > destWidth) {
        val heightScale = srcHeight / destHeight
        val widthScale = srcWidth / destWidth

        val sampleScale = if (heightScale > widthScale) {
            heightScale
        } else {
            widthScale
        }

        inSampleSize = Math.round(sampleScale)
    }

    options = BitmapFactory.Options()
    options.inSampleSize = inSampleSize

    // Чтение и создание окончательного растрового изображения
    return BitmapFactory.decodeFile(path, options)
}

// Масштабирование с консервативной оценкой
fun getScaledBitmap(path: String, activity: Activity): Bitmap {
    val size = Point()
    activity.windowManager.defaultDisplay.getSize(size)

    return getScaledBitmap(path, size.x, size.y)
}
```

## Скриншоты приложения.

### Главный экран.
#### Вертикальный режим.
![list-item-crime](/docs/app-screen/criminalListFragment.jpg)

#### Горизонтальный режим.
![list-item-crime](/docs/app-screen/criminalListFragmentLaptop.jpg)

------------

### Новое преступление.
![new-crime](/docs/app-screen/new-crime.jpg)

------------

### Заполненное преступление.
![ready-crime](/docs/app-screen/ready-crime.jpg)

------------

### Отображение снимка преступления.
![fullscreen-photo-crime](/docs/app-screen/fullscreen-photo-crime.jpg)

------------

### Выбор подозреваемого из контактной книги.
![suspect-crime](/docs/app-screen/suspect-crime.jpg)

------------

### Выбор приложения для отправки отчёта.
![report-crime](/docs/app-screen/report-crime.jpg)

------------

### Отправленный отчём в Telegram.
![report-telegram-crime](/docs/app-screen/sended-report-crime.jpg)

------------
