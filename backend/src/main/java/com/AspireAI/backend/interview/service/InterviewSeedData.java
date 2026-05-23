package com.AspireAI.backend.interview.service;

import com.AspireAI.backend.interview.entity.InterviewQuestion;

import java.util.ArrayList;
import java.util.List;

public class InterviewSeedData {

    public static List<InterviewQuestion> getQuestionsToSeed() {
        List<InterviewQuestion> list = new ArrayList<>();

        list.add(InterviewQuestion.builder().companyTarget("ANY").skillArea("DSA-Arrays & Strings")
                .questionText("Explain the difference between static and dynamic arrays. How do dynamic arrays handle resizing behind the scenes?")
                .questionType("CONCEPTUAL").difficultyLevel(2)
                .idealAnswerHint("Static arrays have fixed sizes. Dynamic arrays (e.g., ArrayList in Java, vectors in C++) dynamically double their capacity when full, copying elements to a new memory block. Resizing takes O(N) time but average insertion remains O(1) amortized.")
                .keyConcepts("dynamic resizing, arraylist, capacity, amortized complexity").build());

        list.add(InterviewQuestion.builder().companyTarget("ANY").skillArea("DSA-Arrays & Strings")
                .questionText("How does the two-pointer technique optimize problems like reversing an array or finding a target pair in a sorted array compared to a nested loop?")
                .questionType("CONCEPTUAL").difficultyLevel(3)
                .idealAnswerHint("The two-pointer technique uses two indices (usually start and end) to scan data from both sides simultaneously. It reduces quadratic O(N^2) time complexities to linear O(N) by making decisions in a single pass without redundant nested scans.")
                .keyConcepts("two-pointers, linear time, nested loops optimization").build());

        list.add(InterviewQuestion.builder().companyTarget("ANY").skillArea("DSA-Trees & Graphs")
                .questionText("Contrast Depth-First Search (DFS) and Breadth-First Search (BFS). In which scenario is BFS strictly preferred over DFS?")
                .questionType("CONCEPTUAL").difficultyLevel(3)
                .idealAnswerHint("DFS uses a stack (recursion) to explore deep paths first. BFS uses a queue to explore level-by-level. BFS is strictly preferred when finding the shortest path in an unweighted graph, as it reaches closer nodes before exploring farther ones.")
                .keyConcepts("DFS, BFS, stack, queue, shortest path, unweighted graph").build());

        list.add(InterviewQuestion.builder().companyTarget("ANY").skillArea("DSA-Trees & Graphs")
                .questionText("Explain what a Binary Search Tree (BST) is. Why does lookup take O(log N) in a balanced BST but can degrade to O(N) in an unbalanced one?")
                .questionType("CONCEPTUAL").difficultyLevel(3)
                .idealAnswerHint("A BST is a node-based binary tree where left child < root < right child. Lookup in a balanced BST halves the search space at each level, leading to logarithmic O(log N) height. In an unbalanced tree, it can degrade to a linear chain (linked list) taking O(N) time.")
                .keyConcepts("BST, binary search tree, height balance, skew tree, tree traversal").build());

        list.add(InterviewQuestion.builder().companyTarget("ANY").skillArea("DSA-Dynamic Programming")
                .questionText("What is Dynamic Programming? Describe the difference between Memoization (top-down) and Tabulation (bottom-up) approaches.")
                .questionType("CONCEPTUAL").difficultyLevel(4)
                .idealAnswerHint("DP solves complex problems by breaking them into overlapping subproblems. Memoization is top-down, using recursion and caching results in a map/array. Tabulation is bottom-up, solving smaller subproblems first and building a table iteratively to avoid recursive call stack overhead.")
                .keyConcepts("dynamic programming, memoization, tabulation, top-down, bottom-up").build());

        list.add(InterviewQuestion.builder().companyTarget("ANY").skillArea("CS-DBMS")
                .questionText("Explain the purpose of database normalization. What are the criteria for a database to be in Third Normal Form (3NF)?")
                .questionType("CONCEPTUAL").difficultyLevel(3)
                .idealAnswerHint("Normalization minimizes redundancy and updates anomalies. 3NF requires: 1) Being in 2NF (no partial dependencies), and 2) No transitive dependencies (every non-prime attribute must depend only on the primary key, 'the key, the whole key, and nothing but the key').")
                .keyConcepts("normalization, 1NF, 2NF, 3NF, transitive dependency, primary key").build());

        list.add(InterviewQuestion.builder().companyTarget("ANY").skillArea("CS-DBMS")
                .questionText("When would you choose a NoSQL database like MongoDB over a traditional relational database (RDBMS) like MySQL?")
                .questionType("CONCEPTUAL").difficultyLevel(3)
                .idealAnswerHint("NoSQL is preferred for dynamic/flexible schemas, high volume of unstructured/semi-structured data, and easy horizontal scaling (sharding). RDBMS is preferred when ACID compliance, strict schemas, and complex relations/joins are crucial.")
                .keyConcepts("RDBMS, NoSQL, MySQL, MongoDB, ACID properties, horizontal scaling").build());

        list.add(InterviewQuestion.builder().companyTarget("ANY").skillArea("CS-OS")
                .questionText("Explain the concept of virtual memory. What is paging, and how does a page fault occur?")
                .questionType("CONCEPTUAL").difficultyLevel(3)
                .idealAnswerHint("Virtual memory lets processes execute with more memory than physically available by mapping logical addresses to physical RAM. Paging divides memory into fixed blocks. A page fault occurs when a process accesses a page that is not currently loaded in physical RAM, forcing the OS to fetch it from disk.")
                .keyConcepts("virtual memory, paging, page fault, RAM mapping, memory management unit").build());

        list.add(InterviewQuestion.builder().companyTarget("ANY").skillArea("Web-APIs")
                .questionText("What is REST, and what are the primary HTTP methods used in RESTful services? Describe the difference between PUT and PATCH.")
                .questionType("CONCEPTUAL").difficultyLevel(3)
                .idealAnswerHint("REST is an architectural style for designing networked applications. Key methods are GET (read), POST (create), PUT (replace), PATCH (modify), and DELETE. PUT replaces the entire resource representation, while PATCH applies partial updates to it.")
                .keyConcepts("REST, RESTful API, GET, POST, PUT, PATCH, HTTP status codes").build());

        list.add(InterviewQuestion.builder().companyTarget("ANY").skillArea("Web-Security")
                .questionText("How does JWT (JSON Web Token) authentication work? What are the key security practices to secure JWTs?")
                .questionType("CONCEPTUAL").difficultyLevel(4)
                .idealAnswerHint("JWT is a stateless token representing user claims. A server signs it with a secret key and sends it to the client. The client sends it back in the Authorization header. Security practices include: using short expiration, storing them in HttpOnly cookies to prevent XSS, validating signatures on every request, and using HTTPS.")
                .keyConcepts("JWT, JSON Web Token, stateless auth, HttpOnly cookies, XSS, security validation").build());

        list.add(InterviewQuestion.builder().companyTarget("ANY").skillArea("AI-ML")
                .questionText("What are vector embeddings, and how do they enable semantic search in artificial intelligence?")
                .questionType("CONCEPTUAL").difficultyLevel(4)
                .idealAnswerHint("Vector embeddings convert unstructured text into high-dimensional numerical vectors where distance (e.g., Cosine Similarity) represents meaning. This enables semantic search to find items with similar concepts, even if they use completely different keywords.")
                .keyConcepts("vector embeddings, cosine similarity, semantic search, vector store, spring ai").build());

        list.add(InterviewQuestion.builder().companyTarget("ANY").skillArea("AI-ML")
                .questionText("Explain the difference between Supervised and Unsupervised Learning. Give a common real-world example of each.")
                .questionType("CONCEPTUAL").difficultyLevel(3)
                .idealAnswerHint("Supervised learning trains on labeled data (e.g., predicting house prices based on size). Unsupervised learning finds hidden patterns in unlabeled data (e.g., clustering active customers into behavioral segments for marketing target campaigns).")
                .keyConcepts("supervised learning, unsupervised learning, labeling, regression, clustering").build());

        list.add(InterviewQuestion.builder().companyTarget("ANY").skillArea("HR-Behavioral")
                .questionText("Describe a challenging technical project you worked on. What obstacles did you face, and how did you overcome them?")
                .questionType("BEHAVIORAL").difficultyLevel(2)
                .idealAnswerHint("A good behavioral answer uses the STAR format: Situation, Task, Action, and Result. It highlights technical problem solving, resourcefulness, teamwork, and quantifies the final outcome/success metrics.")
                .keyConcepts("STAR method, project delivery, problem solving, technical challenges").build());

        list.add(InterviewQuestion.builder().companyTarget("ANY").skillArea("HR-Behavioral")
                .questionText("Tell me about a time you had a conflict with a team member during a group project. How did you handle it?")
                .questionType("BEHAVIORAL").difficultyLevel(3)
                .idealAnswerHint("Use STAR: explain the constructive conflict (disagreement on architecture or tasks), how you actively listened, stayed professional, discussed trade-offs logically, reached a consensus, and delivered a successful project.")
                .keyConcepts("STAR method, team conflict resolution, active listening, professional consensus").build());

        return list;
    }

    public static List<JobPostingSeed> getJobPostingsToSeed() {
        List<JobPostingSeed> seeds = new ArrayList<>();

        seeds.add(new JobPostingSeed("TCS Digital", "Systems Engineer - Digital Trainee", "India - Remote",
                "Java, Spring Boot, REST APIs, SQL, OOP, Agile Methodologies", "Fresher", "7.0 LPA", "https://careers.tcs.com",
                "TCS Digital is hiring Software Engineers who excel in modern application development. Requirements include: solid understanding of object-oriented design in Java or C++, building scalable REST APIs using Spring Boot framework, writing optimized SQL queries, understanding database normalization concepts in RDBMS, and participating in Agile sprints. Strong analytical skills and familiarity with software version control like Git are mandatory."));

        seeds.add(new JobPostingSeed("Infosys SP", "Specialist Programmer", "Bangalore, India",
                "Python, DSA, Django, PostgreSQL, Cloud Computing, Object-Oriented Analysis", "0-2 Years", "9.5 LPA", "https://careers.infosys.com",
                "Infosys Specialist Programmer role focuses on complex system design and execution. Requirements include: deep understanding of Data Structures and Algorithms (including Trees, Graphs, and Dynamic Programming), expert-level knowledge of Python or Java, database administration in PostgreSQL, and deploying systems on Cloud infrastructure (AWS/Azure). Must have hands-on experience solving logical problems and building robust systems."));

        seeds.add(new JobPostingSeed("General Placement Prep", "Software Development Engineer", "India - Hybrid",
                "Data Structures, Algorithms, DBMS, Operating Systems, Computer Networks", "Fresher", "8-12 LPA", "https://aspireai.com",
                "General placement guidance pathway for software engineering students. Key learning areas: mastery in basic Data Structures (Arrays, Linked Lists, Stacks, Queues), tree/graph traversals (DFS/BFS), understanding memory management, virtual memory, paging, and page fault processes in modern Operating Systems. Strong conceptual understanding of Relational Database systems, SQL queries, transaction properties, and networks (TCP/IP model)."));

        seeds.add(new JobPostingSeed("Full-Stack Developer Path", "Full-Stack Developer", "India - Remote",
                "React.js, Node.js, Express, Spring Boot, MySQL, MongoDB, JWT Security, REST APIs", "Fresher", "6.5 LPA", "https://aspireai.com",
                "Career path focused on Full-Stack application architectures. Topics covered: frontend development using React.js hooks and component cycles, state management, and custom CSS styling. Backend programming using Spring Boot or Node.js to design RESTful controllers, securing endpoints using JWT (JSON Web Tokens) stateless sessions, storing data in MySQL databases, and mapping relations with ORM tools. Understanding of CI/CD and deployment basics."));

        seeds.add(new JobPostingSeed("AI & ML Engineer Guidance", "AI / Machine Learning Engineer", "Hyderabad, India",
                "Python, TensorFlow, PyTorch, Vector Embeddings, PgVector, LLMs, Semantic Search", "0-2 Years", "12.0 LPA", "https://aspireai.com",
                "Specialized career track for modern AI/ML applications. Skills focused: developing machine learning pipelines using Python, manipulating data, and training models using PyTorch or TensorFlow. Creating semantic search applications utilizing Vector Embeddings, configuring Postgres PgVector, and executing similarity searches. Harnessing large language models (LLMs) like Gemini or GPT through system prompt engineering."));

        seeds.add(new JobPostingSeed("Data Structures & Algorithms Prep", "Algorithms Developer", "Bangalore, India",
                "Data Structures, Arrays, Dynamic Programming, Trees, Graphs, Sorting Algorithms", "Fresher", "10.0 LPA", "https://aspireai.com",
                "Targeted track for core algorithmic and system engineering interviews. Topics included: array manipulation techniques, dynamic capacity resizing, optimized pointer traversals, DFS/BFS graph scans, minimizing recursion overhead through Dynamic Programming tabulations or memoizations, and sorting/searching time complexity optimizations."));

        seeds.add(new JobPostingSeed("Core Computer Science Freshers", "CS Technical Associate", "India - Hybrid",
                "Operating Systems, DBMS, SQL, Normalization, Networks, Computer Architectures", "Fresher", "6.0 LPA", "https://aspireai.com",
                "Foundational track for core computer science placements. Areas covered: database design, relational schemas, database normalization (1NF, 2NF, 3NF), ACID transaction properties. Operating System concepts including virtual memory paging, process scheduling, page fault exceptions, and networking layers."));

        return seeds;
    }

    public static class JobPostingSeed {
        public String companyName, roleTitle, location, requiredSkills, experienceRange, salaryRange, sourceUrl, description;

        public JobPostingSeed(String companyName, String roleTitle, String location, String requiredSkills,
                       String experienceRange, String salaryRange, String sourceUrl, String description) {
            this.companyName = companyName; this.roleTitle = roleTitle; this.location = location;
            this.requiredSkills = requiredSkills; this.experienceRange = experienceRange;
            this.salaryRange = salaryRange; this.sourceUrl = sourceUrl; this.description = description;
        }
    }
}
