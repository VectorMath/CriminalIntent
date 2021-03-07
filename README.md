# CriminalIntent

[TOC]

## Описание проекта
**CriminalIntent** является pet-проектом, которое было написано в ходе обучения по книге "Android. Программирование для профессионалов 4-е издание."

Приложение преднозначено для хранения информации об офисных преступлениях, пользователь создаёт запись о преступлениях с заголовком, датой и фотографией. Так же можно выбрать подозреваемого в адресной книге и отправить отчёт на электронную почту, соц.сеть или мессенджер.

## Что использовалось при разработке
- Язык программирования **Kotlin**

- **Отображение данных**
-- RecyclerView
-- ViewModel
- **Для создания локальной БД**
-- Room
-- LiveData

- Диалоговые окна

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
// ВСТАВИТЬ ПИКЧУ
##### fragment_crime_list.xml
Layout, где храниться список элементов RecyclerView.
##### fragment_dialog.xml
Фрагмент увеличенной фотографии конкретного преступления.
##### list_item_crime.xml
Внешний вид одного из элементов преступления в списке RecyclerView.
// ВСТАВИТЬ ПИКЧУ

#### Controllers

------------


##### CrimeFragment.kt
Отвечает за весь функционал, который представлен в **fragment_crime.xml.**

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

- **private inner class CrimeHolder(view: View)** -- внутренний класс, который наследуется от класса **RecyclerView.ViewHolder** и интрефейса **View.OnClickListener** реализует заполнение элемента списка в зависимости от общих данных и данных экземпляра класса **Crime** а так же накидывает событие нажатия на элемент, которое позволяет подробнее рассмотреть выбранное преступление, либо же отредактировать его(проще говоря, переход на фрагмент **CrimeFragment**).

- **private inner class CrimeAdapter(var crimes: List<Crime>)** -- Внутренний класс, который реализует функционал RecyclerView.Adapter, позволяющий заполнить список RecyclerView имеющимися данными с помощью класса **CrimeHolder**, а так же обновить и добавить в него новые преступления. Имеет следующие методы:
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
------------

### Singletone
##### CrimeRepository.kt

## Реализация базы данных
### Интерфейс CrimeDao
### CrimeDataBase.kt
### CrimeTypeConverters.kt

## Другие файлы
### CriminalIntentApplication.kt
### CrimeListViewModel.kt
### CrimeDetailViewModel.kt
### PictureUtils.kt

## Скриншоты приложения