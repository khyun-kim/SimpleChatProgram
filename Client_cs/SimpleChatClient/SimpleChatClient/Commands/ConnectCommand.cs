﻿namespace SimpleChatClient.Commands
{
    using System;
    using System.Windows.Input;

    using SimpleChatClient.ViewModels;

    internal class ConnectCommand : ICommand
    {
        public ConnectCommand(LogOnWindowViewModel viewModel)
        {
            _ViewModel = viewModel;
        }

        private LogOnWindowViewModel _ViewModel;

        #region ICommand Member
        public event EventHandler CanExecuteChanged
        {
            add { CommandManager.RequerySuggested += value; }
            remove { CommandManager.RequerySuggested -= value; }
        }

        public bool CanExecute(object parameter)
        {
            return true;
        }

        public void Execute(object parameter)
        {
            _ViewModel.Connect();
        }
        #endregion
    }
}
