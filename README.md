# IELTS Writing Practice & Feedback

A JavaFX desktop application that provides AI-powered feedback for IELTS Writing tasks. Practice both Task 1 and Task 2 with automatically generated questions and receive detailed band scores based on official IELTS criteria.

## Features

- **Authentic Practice Environment**: Simulates real IELTS exam conditions with timed writing sessions
- **AI-Generated Questions**: Creates realistic IELTS Writing questions using Groq's Llama 3.3 70B model
- **Comprehensive Feedback**: Evaluates your writing across all four IELTS criteria:
  - Task Achievement/Response
  - Coherence & Cohesion
  - Lexical Resource
  - Grammatical Range & Accuracy
- **Band 8-9 Sample Answers**: Get model responses for each question to learn from
- **Progress Tracking**: Real-time word count and timer to help you manage your writing
- **Feedback Reports**: Automatically saves detailed reports to your local `IELTS` folder

## Screenshots

### Task Selection
Choose between Task 1 (describing visual information) or Task 2 (essay writing).

### Writing Interface
- 20-minute timer for Task 1 / 40-minute timer for Task 2
- Real-time word count with progress indicator
- Clean, distraction-free writing environment

### Feedback Report
- Overall band score
- Individual scores for each criterion
- Areas for improvement
- Sample answer (Band 8-9 level)

## Requirements

- **Java**: JDK 11 or higher
- **JavaFX**: 17 or higher
- **Groq API Key**: Free tier available at [groq.com](https://groq.com)
- **Internet Connection**: Required for AI question generation and feedback

## Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/ielts-writing-practice.git
   cd ielts-writing-practice
   ```

2. **Set up your Groq API key**
   
   Create an environment variable:
   ```bash
   # Windows (Command Prompt)
   set GROQ_API_KEY=your_api_key_here
   
   # Windows (PowerShell)
   $env:GROQ_API_KEY="your_api_key_here"
   
   # macOS/Linux
   export GROQ_API_KEY=your_api_key_here
   ```

3. **Build the project**
   
   Using Maven:
   ```bash
   mvn clean package
   ```
   
   Or using your IDE (IntelliJ IDEA, Eclipse, etc.)

4. **Run the application**
   ```bash
   java -jar target/ielts-writing-practice.jar
   ```

## Project Structure

```
src/main/java/com/example/ielts/
├── AIService.java              # Handles Groq API calls
├── FeedbackData.java           # Data model for feedback
├── FeedbackFileManager.java    # Saves reports to disk
├── FeedbackScreen.java         # Displays feedback UI
├── IELTSWritingApp.java        # Main application class
├── TaskSelectionScreen.java    # Initial screen
└── WritingTaskScreen.java      # Writing interface

src/main/resources/com/example/ielts/
└── theme.css                   # Application styling
```

## How It Works

1. **Question Generation**: When you select a task, the app sends a prompt to Groq's API requesting an IELTS-style question
2. **Writing Phase**: Write your response within the time limit while monitoring your word count
3. **AI Evaluation**: Upon submission, your answer is sent to the AI for detailed evaluation against IELTS band descriptors
4. **Feedback Display**: Receive scores, improvement suggestions, and a model answer
5. **Report Saving**: All feedback is automatically saved to `~/IELTS/FeedbackReports.txt`

## Technology Stack

- **JavaFX**: Modern UI framework for desktop applications
- **Groq API**: Fast AI inference using Llama 3.3 70B
- **JSON**: Data interchange format for API communication

## Customization

### Change AI Model
Edit `AIService.java` line 12:
```java
private static final String MODEL = "llama-3.3-70b-versatile";
```

### Adjust Time Limits
Edit `WritingTaskScreen.java` line 28:
```java
this.timeInSeconds = taskNumber == 1 ? 1200 : 2400; // 20 or 40 minutes
```

### Modify Styling
Edit `theme.css` to customize colors, fonts, and layout.

## Feedback Reports

Reports are saved to:
- **Windows**: `C:\Users\YourName\IELTS\FeedbackReports.txt`
- **macOS/Linux**: `~/IELTS/FeedbackReports.txt`

Each report includes:
- Date and time
- Question and your answer
- All band scores
- Areas for improvement
- Sample answer

## Limitations

- Requires internet connection for AI features
- AI feedback may not be 100% accurate compared to official IELTS examiners
- Should be used as a practice tool, not a guarantee of exam performance

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

- Powered by [Groq](https://groq.com) for fast AI inference
- IELTS band descriptors are property of the British Council, IDP, and Cambridge Assessment English
- Built with JavaFX

## Support

For issues, questions, or suggestions, please open an issue on GitHub.

---

**Disclaimer**: This is an independent practice tool and is not affiliated with or endorsed by IELTS, the British Council, IDP, or Cambridge Assessment English.
